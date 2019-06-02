<?php

include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';
session_start();

//print_r($_SESSION);
if (isset($_SESSION['messaggioOp'])) {
   echo $_SESSION['messaggioOp'];
   unset($_SESSION['messaggioOp']);
}

$control = empty($listaEntita);
if ($control == "1") {
   $listaEntita = $_SESSION['listaEntita'];
   $entita_numero = estrazioneEntita($listaEntita);
   $nomeEntita = $entita_numero[0];
   $numeroRandom = $entita_numero[1];
   $listaFeature = loadFeature($nomeEntita);
   $listaClassificazioni = loadClassificazioni($nomeEntita);
   $listaClassi = loadClassi($listaClassificazioni);
}

function estrazioneEntita($listaEntita) {

   if (!empty($listaEntita)) {
      $maxNumber = sizeof($listaEntita);
      //genero un numero casuale che sceglierà l'entità da far valutare all'utente
      $numeroRandom = rand(0, $maxNumber - 1);
      $nomeEntita = $listaEntita[$numeroRandom]['nomeEntita'];

      $entita = ["$nomeEntita", "$numeroRandom"];

      return $entita;
   }
}

function loadFeature($nomeEntita) {
   include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';

   //interrogo il db per le feature collegate all'entità estratta
   $query = "SELECT * FROM feature_value WHERE NomeEntita = ?";
   $stmt = $pdo->prepare($query);
   $stmt->bindParam(1, $nomeEntita);
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();

   foreach ($resp as $row) {
      $listaFeature[] = array('nomeFeature' => $row['NomeFeature'], 'featureValue' => $row['FeatureValue']);
   }

   return $listaFeature;
}

function loadClassificazioni($nomeEntita) {
   include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';

   //interrogo il db per le classificazioni collegate all'entità estratta
   $query = "SELECT * FROM annotazione_macchina WHERE NomeEntita = ?";
   $stmt = $pdo->prepare($query);
   $stmt->bindParam(1, $nomeEntita);
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();

   foreach ($resp as $row) {
      $listaClassificazioni[] = array('classificazione' => $row['NomeClassificazione'], 'valoreClassificazione' => $row['ValoreClassificazione']);
   }

   return $listaClassificazioni;
}

function loadClassi($listaClassificazioni) {
   include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';

   foreach ($listaClassificazioni as $row) {
      //interrogo il db per le classificazioni collegate all'entità estratta
      $query = "SELECT * FROM classe_classificazione WHERE NomeClassificazione = ?";
      $stmt = $pdo->prepare($query);
      $stmt->bindParam(1, $row['classificazione']);
      $stmt->execute();
      $resp = $stmt->fetchAll();
      $stmt->closeCursor();

      $classi = [];
      foreach ($resp as $row2) {
         array_push($classi, $row2['Classe']);
      }

      $listaClassi = array('classificazione' => $row['classificazione'], 'classi' => $classi);
      $classi = [];
   }

   return $listaClassi;
}

//print_r($listaFeature);
//se l'utente clicca logout chiudo la sessione, azzero la variabile session['username'] e rimando alla home
if (isset($_POST['action']) and $_POST['action'] == 'LOGOUT') {
   session_destroy();
   unset($_SESSION['username']);
   redirect("home.php");
   exit();
}
//se l'utente clicca invia feed raccolgo i dati inserite e li salvo nel database
if (isset($_POST['action']) and $_POST['action'] == 'INVIA FEED') {
   $nomeAutore = $_SESSION['username'];
   $currentTimestamp = time();
   $data = date("Y/m/d", $currentTimestamp);
   $entita = $_POST['nomeEntita'];
   $numEstratto = $_POST['numEstratto'];
   $counter = 0;
   //apro un ciclo in cui raccolgo le valutazioni per ogni classificazione esistente e faccio un salvataggio nel db 
   //per ogni classificazione
   foreach ($listaClassificazioni as $row) {
      $nomeClassificazione = $_POST['classificazione' . $counter];
      $valoreClassificazione = $_POST['valoreClassificazione' . $counter];
      $valutazione = $_POST['radioOptions' . $counter];

      //echo $data, $nomeAutore, $entita, $nomeClassificazione, $valoreClassificazione, $valutazione;

      $query = "INSERT INTO annotazione_utente(Data, NomeAutore, NomeEntita, NomeClassificazione, ValoreClassificazione, Valutazione)VALUES(?,?,?,?,?,?)";
      $stmt = $pdo->prepare($query);
      $stmt->bindParam(1, $data);
      $stmt->bindParam(2, $nomeAutore);
      $stmt->bindParam(3, $entita);
      $stmt->bindParam(4, $nomeClassificazione);
      $stmt->bindParam(5, $valoreClassificazione);
      $stmt->bindParam(6, $valutazione);
      if ($stmt->execute()) {
         $stmt->closeCursor();

         $counter++;
         //print_r($listaEntita);
         array_splice($listaEntita, $numEstratto, 1);
         $_SESSION['listaEntita'] = $listaEntita;
         //print_r($_SESSION['listaEntita']);
         $_SESSION['messaggioOp'] = "<script> alert('Grazie! Feedback inviato correttamente!'); </script>";

         redirect("listaFunzioni.php");
      }
   }
}

if (isset($_POST['action']) and $_POST['action'] == 'INVIA ENTITÀ') {
   $nomeEntitaInserita = $_POST['inputNomeEntita'];
   $descrizioneInserita = $_POST['inputDescrizione'];

   $numFeatures = $_POST['numFeatures'];
   //apro un ciclo in cui estratto tutte le feature inserite dall'utente
   for ($i = 1; $i <= $numFeatures; $i++) {
      $nomeFeatureInserita = $_POST['inputNomeFeature' . $i];
      $valFeatureInserita = $_POST['inputValFeature' . $i];
      //se l'array è vuoto inserisco la feature senza fare controlli
      if (empty($listaFeatureInserite)) {
         $listaFeatureInserite[$nomeFeatureInserita] = [$valFeatureInserita];
      } else {
         //controllo che la feature inserita non sia già presente nell'array
         $trovato = "false";
         foreach ($listaFeatureInserite as $key => $row) {
            if (strtolower($key) == strtolower($nomeFeatureInserita)) {
               $trovato = "true";
            }
         }
         //see non è presente la inserisco come nuova feature, altrimenti aggiungo il valore
         //della feature insieme agli altri valori già presenti per quella feature
         if ($trovato == "false") {
            $listaFeatureInserite[$nomeFeatureInserita] = [$valFeatureInserita];
         } else {
            $vFeature = [];
            foreach ($row as $row2) {
               array_push($vFeature, $row2);
            }
            array_push($vFeature, $valFeatureInserita);
            $listaFeatureInserite[$nomeFeatureInserita] = $vFeature;
         }
      }
   }
   //costruisco l'array che verrà codificato in JSON
   $entitaInserita['entityURI'] = $nomeEntitaInserita;
   $entitaInserita['abstract'] = $descrizioneInserita;
   $entitaInserita['features'] = $listaFeatureInserite;

   //print_r($entitaInserita);
   //$fp = fopen('results.json', 'w');
   $print = json_encode($entitaInserita, JSON_PRETTY_PRINT);
   //print_r($print);
   //fwrite($fp, $print);
   //fclose($fp);
   //faccio una chiamata al db per ricevere gli url di ogni server per le classificazioni
   $query = "SELECT * FROM classificazione";
   $stmt = $pdo->prepare($query);
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();

   foreach ($resp as $row) {
      $listaUrl[] = array('classificazione' => $row['NomeClassificazione'], 'url' => $row['UrlServer']);
   }

   foreach ($listaUrl as $riga) {
      //istanzio i parametri per la chiamata al server di classificazione
      $url = $riga['url'];

      $options = array(
          'http' => array(
              'header' => "Content-Type: application/json\r\n" .
              "Accept: application/json\r\n",
              'method' => 'POST',
              'content' => $print
          )
      );

      $context = stream_context_create($options);
      $result = file_get_contents($url, false, $context);

      if ($result === FALSE) {
         echo "<script> alert('Errore con la chiamata al server di classificazione!'); </script>";
         exit();
      } else {
         //decodifico il risultato prodotto dal server
         $json_result = json_decode($result, true);

         //estraggo i dati dal file json che ha prodotto il server di classificazione
         if (empty($json_result)) {
            echo "<script> alert('Errore! Il tentativo di classificazione non ha prodotto alcun risultato!'); </script>";
         } else {
            foreach ($json_result as $key => $row) {
               if (strtolower($key) == "entityuri") {
                  $json_entita = $row;
               }
               if (strtolower($key) == "abstract") {
                  $json_descrizione = $row;
               }
               if (strtolower($key) == "features") {
                  $json_features = $row;
               }
               if (strtolower($key) == "annotations") {
                  foreach ($row as $key2 => $row2) {
                     if (is_array($row2)) {
                        foreach ($row2 as $key3 => $row3) {
                           if (strtolower($key3) == "method") {
                              $json_metodo = $row3;
                           }
                           if (strtolower($key3) == "confidence") {
                              $json_confidence = $row3;
                           }
                           if (strtolower($key3) == "label") {
                              $json_risultatoClassificazione = $row3;
                           }
                           if (strtolower($key3) == "classification") {
                              foreach ($row3 as $key4 => $row4) {
                                 if (strtolower($key4) == "name") {
                                    $json_nomeClassificazione = $row4;
                                 }
                                 if (strtolower($key4) == "classes") {
                                    $json_classi = $row4;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
            $risultatoClassificazione[] = array('classificazione' => $json_nomeClassificazione, 'metodo' => $json_metodo, 'confidence' => $json_confidence, 'risultato' => $json_risultatoClassificazione, 'classi' => $json_classi);
         }
      }
   }

   //setto la variabile showModal in modo che il popup con i risultati venga visualizzato
   $showModal = "1";
}

if (isset($_POST['action']) and $_POST['action'] == 'Invia Feed') {
   //$valoreFeed = $_POST['rispostaRisultato'];
   $uEntita = $_POST['json_entita'];
   $uDescrizione = $_POST['json_descrizione'];
   $uFeatures = unserialize($_POST['json_features']);
   $uRisultatiClassificazioni = unserialize($_POST['json_risultatiClassificazioni']);
   /*
     $uMetodo = $_POST['json_metodo'];
     $uNomeClassificazione = $_POST['json_nomeClassificazione'];
     $uRisultato = $_POST['json_risultato'];
     $uClassi = $_POST['json_classi'];
    * 
    */
   $currentTimestamp = time();
   $data = date("Y/m/d", $currentTimestamp);
   //$risposta_utente = $_POST['rispostaRisultato'];

   //faccio le chiamate al DB per salvare i dati inseriti dall'utente
   $origine = "User";
   //salvo l'entità
   $query = "INSERT INTO entita(NomeEntita, Descrizione, Origine) VALUES(?, ?, ?)";
   $stmt = $pdo->prepare($query);
   $stmt->bindParam(1, $uEntita);
   $stmt->bindParam(2, $uDescrizione);
   $stmt->bindParam(3, $origine);
   if ($stmt->execute()) {
      $stmt->closeCursor();

      foreach ($uFeatures as $key => $row) {
         //salvo le features
         $query = "INSERT INTO feature(NomeFeature, NomeEntita) VALUES(?, ?)";
         $stmt = $pdo->prepare($query);
         $stmt->bindParam(1, $key);
         $stmt->bindParam(2, $uEntita);
         if ($stmt->execute()) {
            $stmt->closeCursor();

            //salvo il valore delle feature
            foreach ($row as $row2) {
               $query = "INSERT INTO feature_value(FeatureValue, NomeFeature, NomeEntita) VALUES(?, ?, ?)";
               $stmt = $pdo->prepare($query);
               $stmt->bindParam(1, $row2);
               $stmt->bindParam(2, $key);
               $stmt->bindParam(3, $uEntita);
               $stmt->execute();
               $stmt->closeCursor();
            }
         }
      }
      
      $counter = 0;
      foreach ($uRisultatiClassificazioni as $row) {
         $risposta_utente = $_POST['rispostaRisultato' . $counter];
         //salvo il feedback espresso dall'utente e dal server di classificazione
         $query = "INSERT INTO annotazione_macchina(Data, Metodo, NomeEntita, NomeClassificazione, ValoreClassificazione,
                   Confidence) VALUES(?, ?, ?, ?, ?, ?)";
         $stmt = $pdo->prepare($query);
         $stmt->bindParam(1, $data);
         $stmt->bindParam(2, $row['metodo']);
         $stmt->bindParam(3, $uEntita);
         $stmt->bindParam(4, $row['classificazione']);
         $stmt->bindParam(5, $row['risultato']);
         $stmt->bindParam(6, $row['confidence']);
         $stmt->execute();
         $stmt->closeCursor();

         $query = "INSERT INTO annotazione_utente(Data, NomeAutore, NomeEntita, NomeClassificazione, ValoreClassificazione, Valutazione) VALUES(?, ?, ?, ?, ?, ?)";
         $stmt = $pdo->prepare($query);
         $stmt->bindParam(1, $data);
         $stmt->bindParam(2, $_SESSION['username']);
         $stmt->bindParam(3, $uEntita);
         $stmt->bindParam(4, $row['classificazione']);
         $stmt->bindParam(5, $row['risultato']);
         $stmt->bindParam(6, $risposta_utente);
         $stmt->execute();
         $stmt->closeCursor();
      }


      $_SESSION['messaggioOp'] = "<script> alert('Grazie! Entità inserita correttamente!'); </script>";
      redirect("listaFunzioni.php");
   }
}

function redirect($url) {
   if (!headers_sent()) {
      header('Location: ' . $url);
      exit;
   } else {
      echo '<script type="text/javascript">';
      echo 'window.location.href="' . $url . '"';
      echo '</script>';
      echo '<noscript>';
      echo '<meta http-equiv="refresh" content="2;url=' . $url . '" />';
      echo '</noscript>';
      exit;
   }
}

include "formListaFunzioni.html.php";