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
        <p style="display:none" id="prova">Ciao</p>
        <div class="container">
            <div align="center">
                <p id="title">PROGETTO PER TESI SU INTELLIGENZA ARTIFICIALE</p>
            </div>
            <div align="center" id="sessione">
                <form method="post" action="">
                    <label style="margin-right: 15px;">Sessione di: <?php echo strtoupper($_SESSION['username']); ?></label>
                    <input class="btn btn-primary" type="submit" name="action" value="LOGOUT">
                </form>
            </div>
            <div id="menu">
                <ul class="nav nav-pills center-pills" id="pills-tab" role="tablist">
                    <li class="nav-item">
                        <a class="nav-link active" id="pills-feedback-tab" data-toggle="pill" href="#pills-feedback" role="tab" aria-controls="pills-feedback" aria-selected="true">Esprimi un feedback</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" id="pills-sfidaAlg-tab" data-toggle="pill" href="#pills-sfidaAlg" role="tab" aria-controls="pills-sfidaAlg" aria-selected="false">Sfida algoritmo</a>
                    </li>
                </ul>
                <div class="tab-content" id="pills-tabContent">
                    <div class="tab-pane fade show active shadow-lg p-3 mb-5 bg-white rounded" id="pills-feedback" role="tabpanel" aria-labelledby="pills-feedback-tab">
                       <?php if (isset($nomeEntita)) { ?>
                           <form method="POST" action="">
                               <input type="hidden" value="<?php echo $nomeEntita; ?>" id="nomeEntita" name="nomeEntita">
                               <input type="hidden" value="<?php echo $numeroRandom; ?>" id="numEstratto" name="numEstratto">
                               <label class="label1">ENTITA':  </label><label class="label2"><?php echo $nomeEntita ?></label><br>
                               <?php foreach ($listaFeature as $row) { ?>
                                  <label class="label1"><?php echo strtoupper($row['nomeFeature']) ?>:</label><label class="label2"><?php echo strtolower($row['featureValue']) ?></label><br>
                               <?php } ?>
                               <?php
                               $counter = 0;
                               foreach ($listaClassificazioni as $row) {
                                  ?>
                                  <div class="divlistaFeature">
                                      <input type="hidden" value="<?php echo $row['classificazione']; ?>" name="classificazione<?php echo $counter ?>">
                                      <input type="hidden" value="<?php echo strtolower($row['valoreClassificazione']); ?>" name="valoreClassificazione<?php echo $counter ?>">
                                      <label class="label1"><?php echo strtoupper($row['classificazione']) ?>:</label><label class="label2"><?php echo strtolower($row['valoreClassificazione']) ?></label>
                                      <div style="float:right">
                                          <div class="form-check form-check-inline" style="margin-left: 55px;">
                                              <input class="form-check-input" type="radio" name="radioOptions<?php echo $counter ?>" id="radioCorretto" value="True" required>
                                              <label class="form-check-label label2" for="inlineRadio1">TRUE</label>
                                          </div>
                                          <div class="form-check form-check-inline">
                                              <input class="form-check-input" type="radio" name="radioOptions<?php echo $counter ?>" id="radioNonCorretto" value="False">
                                              <label class="form-check-label label2" for="inlineRadio2">FALSE</label>
                                          </div>
                                      </div>
                                  </div>
                                  <?php
                                  $counter++;
                               }
                               ?>
                               <label class="label1">WIKIPEDIA:</label><label class="label2"><a href="#">Link alla pagina wikipedia</a></label>
                               <div align="center" style="margin-top: 30px;">
                                   <input type="submit" class="btn btn-primary" name="action" value="INVIA FEED">
                               </div>
                           </form>
                        <?php } else { ?>
                           <h4 class="messaggioInfo" align="center">Hai espresso tutti i feedback possibili, grazie!</h4>
                        <?php } ?>
                    </div>
                    <div class="tab-pane fade shadow p-3 mb-5 bg-white rounded" id="pills-sfidaAlg" role="tabpanel" aria-labelledby="pills-sfidaAlg-tab" align="center">
                        <form method="POST" action="">
                            <div style="justify-content: center;">
                                <div class="form-group">
                                    <label for="inputNomeEntita" class='label3'>NOME ENTITA'</label>
                                    <input style="width:50%;" type="text" class="form-control" name="inputNomeEntita" id="inputNomeEntita" placeholder="Digitare il nome dell'entità" required>
                                </div>
                                <div class="form-group">
                                    <label for="inputDescrizione" class='label3'>DESCRIZIONE</label>
                                    <textarea class="form-control" name="inputDescrizione" id="inputDescrizione" rows="10" placeholder="Inserire informazioni che descrivino l'entità" required></textarea>
                                </div>
                                <div class='controls'>
                                    <div class='input-group featureGroup'>
                                        <div class='input-group-prepend'>
                                            <div class='input-group-text'>NOME FEATURE</div>
                                        </div>
                                        <input type='text' class='form-control' name="inputNomeFeature1" id='inputNomeFeature' placeholder='Digitare il nome della feature' required>
                                        <div class='input-group-prepend'>
                                            <div class='input-group-text'>VALORE</div>
                                        </div>
                                        <input type='text' class='form-control' name="inputValFeature1" id='inputValFeature' placeholder='Digitare il valore della feature' required>
                                        <button class="btn btn-success btn-add" type="button">
                                            <span>+</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <?php $counter2 = 1; ?>
                            <input type="hidden" value="<?php echo $counter2; ?>" id="numFeatures" name="numFeatures">
                            <input type="submit" class="btn btn-primary" style="margin-top: 20px;" name="action" value="INVIA ENTITÀ"> 
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- CODICE PER IL POPUP CON I RISULTATI DELL'ENTITA' INSERITA -->
        <form method="post" action="">
            <div class="modal fade" id="modalRisultati" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
                 aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header text-center">
                            <h4 class="modal-title w-100 font-weight-bold messaggioInfo">RESULT</h4>
                            <!--
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                            -->
                        </div>
                        <div class="modal-body mx-3">
                            <div class="md-form mb-5" style="text-align: center">
                                <label class='label1'>Entità classificata come:</label><br>
                                <label class="label2"><?php echo $json_risultatoClassificazione; ?></label>
                            </div>

                            <div class="md-form mb-4" style="text-align: center">
                                <div class="form-check form-check-inline" style="margin-left: 55px;">
                                    <input class="form-check-input" type="radio" name="rispostaRisultato" value="True" required>
                                    <label class="form-check-label label2" for="inlineRadio1">TRUE</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="rispostaRisultato" value="False">
                                    <label class="form-check-label label2" for="inlineRadio2">FALSE</label>
                                </div>
                            </div>

                        </div>
                        <div class="modal-footer d-flex justify-content-center">
                            <input type="hidden" name="json_entita" value="<?php echo $json_entita ?>">
                            <input type="hidden" name="json_descrizione" value="<?php echo $json_descrizione ?>">
                            <input type="hidden" name="json_features" value="<?php echo htmlspecialchars(serialize($json_features)); ?>">
                            <input type="hidden" name="json_metodo" value="<?php echo $json_metodo ?>">
                            <input type="hidden" name="json_risultato" value="<?php echo $json_risultatoClassificazione ?>">
                            <input type="hidden" name="json_nomeClassificazione"value="<?php echo $json_nomeClassificazione ?>">
                            <input type="hidden" name="json_classi" value="<?php print_r($json_classi) ?>">
                            <input type="submit" class="btn btn-primary" name="action" value="Invia Feed">
                        </div>
                    </div>
                </div>
            </div>
        </form>

        <script>

           $(document).ready(function () {

              $(function ()
              {
                 $(document).on('click', '.btn-add', function (e)
                 {
                    e.preventDefault();
                    if (($('.featureGroup').length + 1) > 5) {
                       alert("Only 5 control-group allowed");
                       return false;
                    }

                    var id = ($('.featureGroup').length + 1).toString();
                    document.getElementById("numFeatures").value = id;
                    //alert(id);
                    var controlForm = $('#pills-sfidaAlg .controls:first'),
                            newEntry = $('<div class="input-group featureGroup" style="margin-top: 20px;">\n\
                                    <div class="input-group-prepend">\n\
                                        <div class="input-group-text">NOME FEATURE</div>\n\
                                    </div>\n\
                                    <input type="text" class="form-control" name="inputNomeFeature' + id + '" id="inputNomeFeature' + id + '" placeholder="Digitare il nome della feature" required>\n\
                                    <div class="input-group-prepend">\n\
                                        <div class="input-group-text">VALORE</div>\n\
                                    </div>\n\
                                    <input type="text" class="form-control" name="inputValFeature' + id + '" id="inputValFeature' + id + '" placeholder="Digitare il valore della feature" required>\n\
                                    <button class="btn btn-danger btn-remove" type="button">\n\
                                        <span>-</span>\n\
                                    </button>\n\
                                    <?php $counter2++ ?>\n\
                                    </div>').appendTo(controlForm);

                    newEntry.find('input').val('');
                 }).on('click', '.btn-remove', function (e)
                 {
                    $(this).parents('.featureGroup:first').remove();

                    e.preventDefault();
                    return false;
                 });
              });
           });
        </script>
        <script>
           $(document).ready(function () {
              //istruzione che fa comparire il modal per visualizzare i risultati dell'entità inserita dall'utente
              var showModal = '<?php echo $showModal; ?>';
              if (showModal == "1") {
                 $("#modalRisultati").modal("show");
              }
           });

        </script>
    </body>
</html>


