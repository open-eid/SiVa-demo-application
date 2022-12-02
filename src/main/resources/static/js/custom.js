/*
 * Copyright 2017 Riigi Infosüsteemide Amet
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

(function ($) {
    hljs.initHighlightingOnLoad();
    Dropzone.autoDiscover = false;

    Dropzone.options.sivaDropzone = {
        maxFiles: 1,
        maxFilesize: 11,
        previewTemplate : '<progress class="progress progress-info progress-striped" id="file-progress" value="0" max="100"></progress>',
        dictDefaultMessage: 'Drop files here or click to browse for upload file'
    };

    var sivaDropzone = new Dropzone('#siva-dropzone');
    sivaDropzone.on('complete', function () {
        sivaDropzone.removeAllFiles();
    });

    sivaDropzone.on('sending', function (file, xhr, formData) {
        $('#result-area, #validation-summery, #validation-warnings, #json-data-files-link, #soap-data-files-link').addClass("hide");
        $('#validation-warning-rows').empty();
        $('#json-data-files-report').empty();
        $('#soap-data-files-report').empty();
        var policy = $('select#policy-select').val();
        var report = $('select#report-select').val();
        var type = $('select#type-select').val();
        var returnDataFiles = $('#return-data-files').prop('checked');
        console.log('Validation policy: ' + policy);
        console.log('Report type: ' + report);
        formData.append("policy", policy);
        formData.append("type", type);
        formData.append("report", report);
        formData.append("encodedFilename", encodeURI(file.name));
        formData.append("returnDataFiles", returnDataFiles);
    });

    sivaDropzone.on('uploadprogress', function (file, progress) {
        for (var i = 0; i <= 100; i++) {
            $('#file-progress').attr('value', i);
        }
    });

    sivaDropzone.on('success', function (file, response) {
        $('#result-area').removeClass('hide');
        $('.nav-tabs a[href="#json"]').tab('show');

        $('#validation-report').text(response.jsonValidationResult);
        $('#validation-report').each(function (i, block) {
            hljs.highlightBlock(block);
        });

        $('#soap-validation-report').text(response.soapValidationResult);
        $('#soap-validation-report').each(function (i, block) {
            hljs.highlightBlock(block);
        });

        if (!jQuery.isEmptyObject(response.jsonDataFilesResult)) {
            $('#json-data-files-link').removeClass("hide");
            $('#json-data-files-report').text(response.jsonDataFilesResult);
            $('#json-data-files-report').each(function (i, block) {
                hljs.highlightBlock(block);
            });
        }

        if (!jQuery.isEmptyObject(response.soapDataFilesResult)) {
            $('#soap-data-files-link').removeClass("hide");
            $('#soap-data-files-report').text(response.soapDataFilesResult);
            $('#soap-data-files-report').each(function (i, block) {
                hljs.highlightBlock(block);
            });
        }

        console.log(response);
        if (response.filename !== '') {
            $('#validation-summery').removeClass('hide');
            $('#document-name').text(response.filename);
            $('#overall-validation-result')
                .removeClass('invalid')
                .removeClass('valid')
                .addClass(response.overAllValidationResult.toLowerCase())
                .text(response.overAllValidationResult);

            if (!jQuery.isEmptyObject(response.validationWarnings)) {
                $('#validation-warnings').removeClass('hide');
                $.each(response.validationWarnings, function(i, value) {
                    $('#validation-warning-rows').append('<li>' + value + '</li>');
                });
            }
        }
    });

    if (Cookies.get('notification') === undefined) {
        $('#notification').show();
    }

    $('#notification').find('button.close').click(function(e) {
        e.preventDefault();
        Cookies.set('notification', 'closed', {path: '/'});
    });
})(jQuery);
