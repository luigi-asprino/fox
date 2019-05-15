<?php

include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';
session_start();

//print_r($_SESSION);
if(isset($_SESSION['messaggioOp'])){
   echo $_SESSION['messaggioOp'];
   echo "ciao";
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
   //per ogni feature esistente
   foreach ($listaClassificazioni as $row) {
      $nomeClassificazione = $row['classificazione'];
      $valoreClassificazione = $row['valoreClassificazione'];
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
         //echo "---------------------------------------------------------------------------------------------------------------";
         //print_r($listaEntita);
         array_splice($listaEntita, $numEstratto, 1);
         $_SESSION['listaEntita'] = $listaEntita;
         //print_r($_SESSION['listaEntita']);
         $_SESSION['messaggioOp'] = "<script> alert('Grazie! Feedback inviato correttamente!'); </script>";
         
         redirect("listaFunzioni.php");
      }
   }

   //redirect("listaFunzioni.php");
}

if (isset($_POST['action']) and $_POST['action'] == 'INVIA ENTITÀ') {
   $nomeEntita = $_POST['inputNomeEntita'];
   $descrizione = $_POST['inputDescrizione'];

   $numFeatures = $_POST['numFeatures'];
   echo $numFeatures;
   for ($i = 1; $i <= $numFeatures; $i++) {
      $nomeFeature = $_POST['inputNomeFeature' . $i];
      $valFeature = $_POST['inputValFeature' . $i];
      $listaFeatureInserite[$nomeFeature] = $valFeature;
   }

   $entitaInserita['nomeEntita'] = $nomeEntita;
   $entitaInserita['descrizione'] = $descrizione;
   $entitaInserita['features'] = $listaFeatureInserite;

   //print_r($entitaInserita);

   $fp = fopen('results.json', 'w');
   $print = json_encode($entitaInserita, JSON_PRETTY_PRINT);
   fwrite($fp, $print);
   fclose($fp);

   //print_r($listaFeatureInserite);
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
