<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css" integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS" crossorigin="anonymous">
        <link rel="stylesheet" href="progetto_tesi_CSS.css">
        <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js" integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js" integrity="sha384-ChfqqxuZUCnJSK3+MXmPNIyE6ZbWh2IMqE241rYiqJxyMiZ6OW/JmZQ5stwEULTy" crossorigin="anonymous"></script>

        <title></title>
    </head>
    <body>
        <div class="container">
            <div align="center">
                <p id="title">PROGETTO PER TESI SU INTELLIGENZA ARTIFICIALE</p>
            </div>
            <div align="center">
                <form method="POST" action="" id="formUtente">
                    <div class='input-group featureGroupHome'>
                        <div class='input-group-prepend'>
                            <div class='input-group-text'>NOME UTENTE</div>
                        </div>
                        <input type='text' class='form-control' name='username' placeholder='Digitare il nome per identificare la sessione' required>
                        <input class="btn btn-primary" type="submit" name="action" value="ACCEDI">
                    </div>
                </form>
                <form method="post" action="" id="formAdmin">
                    <div class='input-group featureGroupHomeAdmin'>
                        <div class='input-group-prepend'>
                            <div class='input-group-text'>PASSWORD</div>
                        </div>
                        <input type='password' class='form-control' name='pass' placeholder='Digita la password per autenticarti come admin' required>
                        <input class="btn btn-primary" type="submit" name="action" value="INVIA">
                    </div>
                </form>
            </div>
        </div>
    </body>
    <script>
       document.getElementById('formAdmin').style.display = 'none';
       document.getElementById('opzioniAdmin').style.display = 'none';
    </script>
    <?php
    if (isset($_POST['action']) and $_POST['action'] == 'ACCEDI') {
       $username = $_POST['username'];
       if (strtolower($username) == "admin") {
          ?>
          <script>
             document.getElementById('formAdmin').style.display = 'visible';
             document.getElementById('formUtente').style.display = 'none';
             document.getElementById('opzioniAdmin').style.display = 'visible';
          </script>
          <?php
       }
    }
    if (isset($_POST['action']) and $_POST['action'] == 'INVIA') {
       $pass = $_POST['pass'];
       if (strtolower($pass) == "progettotesi123") {
          header('location: upload.php');
       } else {
          echo "<script> alert('Errore! Password sbagliata!'); </script>";
       }
    }
    ?>
</html>

