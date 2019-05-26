<?php

include $_SERVER['DOCUMENT_ROOT'] . '/progettoTesi/db.php';
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

if (isset($_POST['action']) and $_POST['action'] == 'ACCEDI') {
   $username = $_POST['username'];
   if (strtolower($username) == "admin") { //utente admin quindi chiedo la password
      //echo "<script> document.getElementById('formAdmin').style.display = 'visible'; </script>";
      echo "<form method='post' action='' id='formAdmin'>
                    <div class='input-group featureGroupHomeAdmin'>
                        <div class='input-group-prepend'>
                            <div class='input-group-text'>PASSWORD</div>
                        </div>
                        <input type='text' class='form-control' name='username' placeholder='Digitare il nome per identificare la sessione' required>
                        <input class='btn btn-primary' type='submit' name='action' value='ACCEDI'>
                    </div>
                </form>";
   } else { //utente normale
      //interrogo il db chiedendo tutte le entità salvate nel db
      $query = "SELECT * FROM entita";
      $stmt = $pdo->prepare($query);
      $stmt->execute();
      $resp = $stmt->fetchAll();
      $stmt->closeCursor();
      foreach ($resp as $row) {
         $entita[] = array('nomeEntita' => $row['NomeEntita'], 'descrizione' => $row['Descrizione']);
      }


      session_start();
      $_SESSION['username'] = $username;
      $_SESSION['listaEntita'] = $entita;
      header('location: listaFunzioni.php');
      exit();
   }
}

if (isset($_POST['action']) and $_POST['action'] == 'DOWNLOAD USERS FEEDBACK') {
   //interrogo il db chiedendo tutte le entità salvate nel db
   $query = "SELECT * FROM annotazioneUtente";
   $stmt = $pdo->prepare($query);
   $stmt->execute();
   $resp = $stmt->fetchAll();
   $stmt->closeCursor();
   foreach ($resp as $row) {
      $annotazioniUtente[] = array('nomeEntita' => $row['NomeEntita'], 'descrizione' => $row['Descrizione']);
   }
}

include "formHome.html.php";

