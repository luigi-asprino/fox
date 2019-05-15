
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
                <form method="post" action="" id="opzioniAdmin">
                    <input class="btn btn-primary" type="submit" name="action" value="LOGOUT">
                </form>
                <form method="post" action="" enctype="multipart/form-data">
                    <div align="center">
                        <div id="divInputFile">
                            Select file JSON to upload:
                            <input type="file" name="fileToUpload" id="fileToUpload">
                            <input id="submitFile" type="submit" class="btn btn-primary" value="Upload file" name="action"><br>
                        </div>
                    </div>
                </form>
                <form method="post" action="">
                    <div align="center">
                        <input class="btn btn-primary" type="submit" name="action" value="DOWNLOAD USERS ENTITY">
                        <input class="btn btn-primary" type="submit" name="action" value="DOWNLOAD USERS FEEDBACK">
                    </div>
                </form>
            </div>
    </body>
</html>

