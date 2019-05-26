<?php

include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';
ini_set('max_execution_time', 500);


session_start();
if (isset($_SESSION['messaggio'])) {
   echo $_SESSION['messaggio'];
   unset($_SESSION['messaggio']);
}





if (isset($_POST['action']) and $_POST['action'] == 'LOGOUT') {
   session_destroy();
   unset($_SESSION['username']);
   redirect("home.php");
   exit();
}


if (isset($_POST['action']) and $_POST['action'] == 'Upload file') {
   $target_dir = "uploads/";
   $target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
   $uploadOk = 1;
   $fileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));

   if ($fileType == "") {
      echo "<script> alert('Errore! Nessun file inserito!'); </script>";
      $uploadOk = 0;
   } else {
      // Check if file already exists
      if (file_exists($target_file)) {
         echo "<script> alert('Errore! File già esistente!'); </script>";
         $uploadOk = 0;
      }

      // Allow certain file formats
      if ($fileType !== "json") {
         echo "<script> alert('Errore! È consentito inserire solo file JSON!'); </script>";
         $uploadOk = 0;
      }

      if ($uploadOk == 0) {
         echo "<script> alert('Errore! Upload non completato!'); </script>";
         // if everything is ok, try to upload file
      } else {
         if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
            // Leggi il file JSON
            $json = file_get_contents('./uploads/' . basename($_FILES["fileToUpload"]["name"]));
            // Decodifica JSON
            $json_data = json_decode($json, true);
            //echo sizeof($json_data);
            $elencoClassi = [];
            // Stampa dati
            try {
               foreach ($json_data as $row) {
                  $elencoFeatures = $row['features'];
                  $elencoAnnotazioni = $row['annotations'];
                  $entita = $row['entityURI'];
                  $descrizione = $row['abstract'];

                  //inserisco l'entita e la descrizione nel db
                  $query = "INSERT INTO entita(NomeEntita, Descrizione) VALUES (?, ?)";
                  $stmt = $pdo->prepare($query);
                  $stmt->bindParam(1, $entita);
                  $stmt->bindParam(2, $descrizione);
                  if ($stmt->execute()) {
                     $stmt->closeCursor();

                     //inserisco le features collegate all'entita nel db
                     for ($i = 0; $i < sizeof($elencoFeatures); $i++) {
                        $key = key($elencoFeatures);
                        $nomeFeature = $key;
                        foreach ($elencoFeatures[$key] as $row2) {
                           $valoreFeature = $row2;
                        }

                        $query = "INSERT INTO feature(NomeFeature, NomeEntita) VALUES (?, ?)";
                        $stmt = $pdo->prepare($query);
                        $stmt->bindParam(1, $nomeFeature);
                        $stmt->bindParam(2, $entita);
                        if ($stmt->execute()) {
                           $stmt->closeCursor();

                           $query = "INSERT INTO feature_value(FeatureValue, NomeFeature, NomeEntita) VALUES (?, ?, ?)";
                           $stmt = $pdo->prepare($query);
                           $stmt->bindParam(1, $valoreFeature);
                           $stmt->bindParam(2, $nomeFeature);
                           $stmt->bindParam(3, $entita);
                           if ($stmt->execute()) {

                              $stmt->closeCursor();

                              next($elencoFeatures);
                           }
                        }
                     }

                     foreach ($elencoAnnotazioni as $annotazione) {
                        for ($i = 0; $i < sizeof($annotazione); $i++) {
                           $key = key($annotazione);
                           if ($key == "method") {
                              $metodo = $annotazione[$key];
                           } else if ($key == "classification") {
                              for ($j = 0; $j < sizeof($annotazione[$key]); $j++) {
                                 $key2 = key($annotazione[$key]);
                                 if ($key2 == "name") {
                                    $array = $annotazione[$key];
                                    $nomeClassificazione = $array[$key2];
                                 } else if ($key2 == "classes") {
                                    //se l'array non è ancora stato inizializzato, salvo tutte le classi nell'array
                                    if (empty($elencoClassi)) {
                                       $elencoClassi = $annotazione[$key];

                                       foreach ($elencoClassi['classes'] as $classe) {
                                          //inserisco le classi nel db
                                          $query = "INSERT INTO classificazione(NomeClassificazione, NomeClasse) VALUES (?, ?)";
                                          $stmt = $pdo->prepare($query);
                                          $stmt->bindParam(1, $elencoClassi['name']);
                                          $stmt->bindParam(2, $classe);
                                          if ($stmt->execute()) {
                                             $stmt->closeCursor();
                                          }
                                       }
                                    } else {

                                       //se l'array è già stato inizializzato precedentemente, controllo che non ci siano
                                       //nuove classificazioni, in quel caso inserisco la nuova classificazione
                                       $nuovoArrayClasse = $annotazione[$key];
                                       if ($nuovoArrayClasse['name'] !== $elencoClassi['name']) {

                                          foreach ($nuovoArrayClasse['classes'] as $classe) {
                                             //inserisco le classi nel db
                                             $query = "INSERT INTO classificazione(NomeClassificazione, NomeClasse) VALUES (?, ?)";
                                             $stmt = $pdo->prepare($query);
                                             $stmt->bindParam(1, $nuovoArrayClasse['name']);
                                             $stmt->bindParam(2, $classe);
                                             if ($stmt->execute()) {
                                                $stmt->closeCursor();
                                             }
                                          }
                                       }
                                    }
                                 }

                                 next($annotazione[$key]);
                              }
                           } else if ($key == "label") {
                              $valoreClassificazione = $annotazione[$key];
                           }
                           next($annotazione);
                        }

                        //salvo nel db le annotazioni di ciascuna entita
                        $currentTimestamp = time();
                        $data = date("Y/m/d", $currentTimestamp);
                        $query = "INSERT INTO annotazione_macchina(Data, Metodo, NomeEntita, NomeClassificazione, ValoreClassificazione) VALUES (?, ?, ?, ?, ?)";
                        $stmt = $pdo->prepare($query);
                        $stmt->bindParam(1, $data);
                        $stmt->bindParam(2, $metodo);
                        $stmt->bindParam(3, $entita);
                        $stmt->bindParam(4, $nomeClassificazione);
                        $stmt->bindParam(5, $valoreClassificazione);
                        if ($stmt->execute()) {
                           $stmt->closeCursor();
                        }
                     }
                  }
               }


               $_SESSION['messaggio'] = "<script> alert('Successo! Il file " . basename($_FILES["fileToUpload"]["name"]) . "è stato caricato!');</script>";
            } catch (Exception $ex) {
               echo "<script> alert('Errore! Upload non completato! " . $ex . "'); </script>";
            }

            //echo "<script> alert('Successo! Il file " . basename($_FILES["fileToUpload"]["name"]) . "è stato caricato!');</script>";

            ob_flush();
            flush();
            sleep(5);
            redirect("upload.php");
         } else {
            echo "<script> alert('Errore! Upload non completato!'); </script>";
         }
      }
   }
}

if (isset($_POST['action']) and $_POST['action'] == 'DOWNLOAD USERS FEEDBACK') {

   include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';
   //interrogo il database per ricevere tutti i feedback dati dagli utenti
   $query = "SELECT * FROM annotazione_utente";
   $stmt = $pdo->prepare($query);
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();

   foreach ($resp as $row) {
      $listaAnnotazioni[] = array('nomeEntita' => $row['NomeEntita'], 'nomeClassificazione' => $row['NomeClassificazione'], 'valoreClassificazione' => $row['ValoreClassificazione'], 'valutazione' => $row['Valutazione']);
   }

   //interrogo il db per ricevere tutte le classificazioni esistenti
   $query = "SELECT * FROM classificazione";
   $stmt = $pdo->prepare($query);
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();

   foreach ($resp as $row) {
      $listaClassificazioni[] = array('nomeClassificazione' => $row['NomeClassificazione'], 'classe' => $row['NomeClasse']);
   }

   $listaClassificazioni2 = $listaClassificazioni;
   $listaClassi = [];
   $classi = [];
   //apro un ciclo per creare un array che mi raggruppi tutte le classi per ogni classificazione
   for ($i = 0; $i < sizeof($listaClassificazioni); $i++) {

      $nomeClassificazione = $listaClassificazioni[$i]['nomeClassificazione'];
      //cerco il nome della classificazione all'interno dell'array delle classi
      if (empty($listaClassi)) {
         $trovato = false;
      } else {
         for ($m = 0; $m < sizeof($listaClassi); $m++) {
            if ($listaClassi[$m]['nomeClassificazione'] == $nomeClassificazione) {
               $trovato = true;
               break;
            }
         }
      }
      //se non lo trovo significa che devo ancora registrare le classi di questa classificazione
      //se invece lo trovo le ho già memorizzate per cui non faccio niente
      if ($trovato == false) {
         //$classi = [$listaClassificazioni[$i]['classe']];
         array_push($classi, $listaClassificazioni[$i]['classe']);

         for ($j = $i + 1; $j < sizeof($listaClassificazioni2); $j++) {
            if ($listaClassificazioni2[$j]['nomeClassificazione'] == $nomeClassificazione) {
               array_push($classi, $listaClassificazioni2[$j]['classe']);
            }
            array_splice($listaClassificazioni2, $j, 1);
            $j--;
         }

         $array['nomeClassificazione'] = $nomeClassificazione;
         $array['classi'] = $classi;

         array_push($listaClassi, $array);
      }
   }

   //print_r($listaClassi);
   //creo un array in cui ho tutte le classi di tutte le classificazioni con un campo in cui inserire il numero di feedback
   $contaClassificazioni = [];
   $feedbackClassi = [];

   for ($i = 0; $i < sizeof($listaClassi); $i++) {
      foreach ($listaClassi[$i]['classi'] as $row) {
         $array2['classe'] = $row;
         $array2['numeroFeedback'] = 0;

         array_push($feedbackClassi, $array2);
      }
      array_push($contaClassificazioni, array('classificazione' => $listaClassi[$i]['nomeClassificazione'], 'classi' => $feedbackClassi));
   }

   //print_r($contaClassificazioni);
   //inizializzo un array uguale a listaAnnotazioni in cui andrò a rimuovere le entita analizzate
   //per il conteggio dei feedback
   $listaAnnotazioni2 = $listaAnnotazioni;

   //inizializzo l'array che conterrà tutte le entità con i relativi conteggi dei feedback
   $listaEntitaFeedback = [];

   //creo un ciclo in cui conto quanti feedback ha ciascuna classe di ciascuna entità
   for ($i = 0; $i < sizeof($listaAnnotazioni); $i++) {
      $entita = $listaAnnotazioni[$i]['nomeEntita'];
      $control = empty($listaEntitaFeedback);
      if ($control == "1") {
         //aggiungo il feedback della prima entita all'array che conta le classificazioni
         for ($k = 0; $k < sizeof($contaClassificazioni); $k++) {
            if (strtolower($contaClassificazioni[$k]['classificazione']) == strtolower($listaAnnotazioni[$i]['nomeClassificazione'])) {
               foreach ($contaClassificazioni[$k]['classi'] as $key => $row) {
                  if (strtolower($row['classe']) == strtolower($listaAnnotazioni[$i]['valoreClassificazione'])) {
                     if (strtolower($listaAnnotazioni[$i]['valutazione']) == "true") {
                        $contaClassificazioni[$k]['classi'][$key]['numeroFeedback'] ++;
                     }
                  }
               }
            }
         }

         //cerco le entità uguali all'interno della lista e le elimino dopo averle analizzate
         for ($j = $i + 1; $j < sizeof($listaAnnotazioni2); $j++) {
            if ($entita == $listaAnnotazioni2[$j]['nomeEntita']) {
               //apro un ciclo in cui cerco la classificazione dell'entita analizzata
               //nell'array che conta i feedback per ogni classificazione
               for ($m = 0; $m < sizeof($contaClassificazioni); $m++) {
                  if (strtolower($contaClassificazioni[$m]['classificazione']) == strtolower($listaAnnotazioni2[$j]['nomeClassificazione'])) {
                     foreach ($contaClassificazioni[$m]['classi'] as $key => $row) {
                        if (strtolower($row['classe']) == strtolower($listaAnnotazioni2[$j]['valoreClassificazione'])) {
                           if (strtolower($listaAnnotazioni2[$j]['valutazione']) == "true") {
                              $contaClassificazioni[$m]['classi'][$key]['numeroFeedback'] ++;
                           }
                        }
                     }
                  }
               }
               array_splice($listaAnnotazioni2, $j, 1);
               $j--;
            }
         }

         //if($entita == "56"){print_r($contaClassificazioni);}
         $feedbackEntita['entityUri'] = $entita;
         $feedbackEntita['feedbacks'] = $contaClassificazioni;

         array_push($listaEntitaFeedback, $feedbackEntita);

         //azzero l'array dei conteggi
         for ($n = 0; $n < sizeof($contaClassificazioni); $n++) {
            foreach ($contaClassificazioni[$n]['classi'] as $key => $row) {
               $contaClassificazioni[$n]['classi'][$key]['numeroFeedback'] = 0;
            }
         }
      } else {
         //controllo se l'entita in questione è già stata inserita nella lista delle entità già conteggiate
         $trovato2 = "false";
         for ($v = 0; $v < sizeof($listaEntitaFeedback); $v++) {
            if ($entita == $listaEntitaFeedback[$v]['entityUri']) {
               $trovato2 = "true";
               break;
            }
         }
         if ($trovato2 == "false") {
            //aggiungo il feedback della prima entita all'array che conta le classificazioni
            for ($k = 0; $k < sizeof($contaClassificazioni); $k++) {
               if (strtolower($contaClassificazioni[$k]['classificazione']) == strtolower($listaAnnotazioni[$i]['nomeClassificazione'])) {
                  foreach ($contaClassificazioni[$k]['classi'] as $key => $row) {
                     if (strtolower($row['classe']) == strtolower($listaAnnotazioni[$i]['valoreClassificazione'])) {
                        //NON ENTRAAA QUAAAA
                        if (strtolower($listaAnnotazioni[$i]['valutazione']) == "true") {
                           $contaClassificazioni[$k]['classi'][$key]['numeroFeedback'] ++;
                        }
                     }
                  }
               }
            }

            //cerco le entità uguali all'interno della lista e le elimino dopo averle analizzate
            for ($j = $i + 1; $j < sizeof($listaAnnotazioni2); $j++) {
               if ($entita == $listaAnnotazioni2[$j]['nomeEntita']) {
                  //apro un ciclo in cui cerco la classificazione dell'entita analizzata
                  //nell'array che conta i feedback per ogni classificazione
                  for ($m = 0; $m < sizeof($contaClassificazioni); $m++) {
                     if (strtolower($contaClassificazioni[$m]['classificazione']) == strtolower($listaAnnotazioni2[$j]['nomeClassificazione'])) {
                        foreach ($contaClassificazioni[$m]['classi'] as $key => $row) {
                           if (strtolower($row['classe']) == strtolower($listaAnnotazioni2[$j]['valoreClassificazione'])) {
                              if (strtolower($listaAnnotazioni2[$j]['valutazione']) == "true") {
                                 $contaClassificazioni[$m]['classi'][$key]['numeroFeedback'] ++;
                              }
                           }
                        }
                     }
                  }
                  array_splice($listaAnnotazioni2, $j, 1);
                  $j--;
               }
            }

            $feedbackEntita['entityUri'] = $entita;
            $feedbackEntita['feedbacks'] = $contaClassificazioni;

            array_push($listaEntitaFeedback, $feedbackEntita);

            //azzero l'array dei conteggi
            for ($n = 0; $n < sizeof($contaClassificazioni); $n++) {
               foreach ($contaClassificazioni[$n]['classi'] as $key => $row) {
                  $contaClassificazioni[$n]['classi'][$key]['numeroFeedback'] = 0;
               }
            }
         }
      }
   }

   $arrayClassi = [];
   $arrayRisultati = [];
   $stampa = [];
   //costruisco l'array che verrà stampato nel file JSON
   for ($i = 0; $i < sizeof($listaEntitaFeedback); $i++) {
      $entita = $listaEntitaFeedback[$i]['entityUri'];
      $arrayStampa['entityURI'] = $entita;

      foreach ($listaEntitaFeedback[$i]['feedbacks'] as $row) {
         $classificazione = $row['classificazione'];
         foreach ($row['classi'] as $row2) {
            array_push($arrayClassi, $row2['classe']);

            $c1['class'] = $row2['classe'];
            $c1['number_of_feedbacks'] = $row2['numeroFeedback'];
            array_push($arrayRisultati, $c1);
         }
         $arrayStampaFeedbacks['classification'] = array('classification_name' => $classificazione, 'classes' => $arrayClassi);
      }



      $arrayStampaFeedbacks['results'] = $arrayRisultati;
      $arrayStampa['feedbacks'] = $arrayStampaFeedbacks;

      array_push($stampa, $arrayStampa);

      //resetto array
      $arrayClassi = [];
      $arrayRisultati = [];
   }

   $fp = fopen('users_feedback.json', 'w');
   $print = json_encode($stampa, JSON_PRETTY_PRINT);
   fwrite($fp, $print);
   fclose($fp);

   $file = "users_feedback.json";
   // Quick check to verify that the file exists
   if (!file_exists($file)) {
      die("File not found");
   } else {
      // Force the download
      header('Content-Disposition: attachment; filename=""' . basename($file) . '""');
      header("Content-Length: " . filesize($file));
      header("Content-Type: application/json;");
      readFile($file);
      exit();
   }
}

function redirect($url) {
   if (!headers_sent()) {
      header('Location: ' . $url);
      exit;
   } else {
      echo '<script type="text/javascript">';
      echo 'window.location.href="' . $url . '";';
      echo '</script>';
      echo '<noscript>';
      echo '<meta http-equiv="refresh" content="5;url=' . $url . '" />';
      echo '</noscript>';
      exit;
   }
}

include "formUpload.html.php";

