<?php

include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';
ini_set('max_execution_time', 500);


session_start();
if(isset($_SESSION['messaggio'])){
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
            echo sizeof($json_data);
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
                        $stmt->execute();
                        $stmt->closeCursor();
                     }
                  }
               }
               
               $_SESSION['messaggio'] = "<script> alert('Successo! Il file " . basename($_FILES["fileToUpload"]["name"]) . "è stato caricato!');</script>";
               
            } catch (Exception $ex) {
               echo "<script> alert('Errore! Upload non completato! ". $ex . "'); </script>";
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
   $query = "SELECT * FROM annotazioni_utente";
   $stmt = $pdo->prepare();
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();
   
   
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

