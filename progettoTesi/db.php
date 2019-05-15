<?php

try
{
  $pdo = new PDO('mysql:host=localhost;dbname=progetto_tesi', 'root');
  $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
  $pdo->exec('SET NAMES "utf8"');
  
}
catch (PDOException $e)
{
  $error = 'Errore, impossibile connettersi al database.';
  include 'error.html.php';
  exit();
}

