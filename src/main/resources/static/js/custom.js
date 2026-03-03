/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

Dropzone.autoDiscover = false;

Dropzone.options.sivaDropzone = {
    maxFiles: 1,
    maxFilesize: 11,
    previewTemplate: '<progress class="progress progress-info progress-striped" id="file-progress" value="0" max="100"></progress>',
    dictDefaultMessage: 'Drop files here or click to browse for upload file'
};

var resultArea = document.getElementById('result-area');
var validationSummary = document.getElementById('validation-summery');
var validationWarnings = document.getElementById('validation-warnings');
var jsonDataFilesLink = document.getElementById('json-data-files-link');
var errorArea = document.getElementById('error');
var validationWarningRows = document.getElementById('validation-warning-rows');
var jsonDataFilesReport = document.getElementById('json-data-files-report');
var requestError = document.getElementById('request-error');
var policySelect = document.getElementById('policy-select');
var reportSelect = document.getElementById('report-select');
var typeSelect = document.getElementById('type-select');
var returnDataFilesCheckbox = document.getElementById('return-data-files');
var validationReport = document.getElementById('validation-report');
var documentName = document.getElementById('document-name');
var overallValidationResult = document.getElementById('overall-validation-result');
var notification = document.getElementById('notification');
var closeNotificationButton = notification.querySelector('button.btn-close');
var responseTabLinks = document.querySelectorAll('#response-tabs .nav-link');
var responseTabPanes = document.querySelectorAll('#result-area .tab-pane');

function isEmpty(value) {
    if (value === null || value === undefined) {
        return true;
    }
    if (typeof value === 'string') {
        return value.length === 0;
    }
    if (Array.isArray(value)) {
        return value.length === 0;
    }
    if (typeof value === 'object') {
        return Object.keys(value).length === 0;
    }
    return false;
}

function getNotificationState() {
    try {
        return sessionStorage.getItem('notification');
    } catch (e) {
        return null;
    }
}

function setNotificationState(value) {
    try {
        sessionStorage.setItem('notification', value);
    } catch (e) {
    }
}

function clearLegacyNotificationState() {
    try {
        localStorage.removeItem('notification');
    } catch (e) {
    }
}

function applyNotificationVisibility() {
    if (getNotificationState() === 'closed') {
        notification.classList.add('hide');
        return;
    }
    notification.classList.remove('hide');
}

clearLegacyNotificationState();
applyNotificationVisibility();

window.addEventListener('pageshow', function () {
    applyNotificationVisibility();
});

function activateResponseTab(targetHash) {
    responseTabLinks.forEach(function (tabLink) {
        var isActive = tabLink.getAttribute('href') === targetHash;
        tabLink.classList.toggle('active', isActive);
    });

    responseTabPanes.forEach(function (tabPane) {
        var isActive = ('#' + tabPane.id) === targetHash;
        tabPane.classList.toggle('active', isActive);
    });
}

responseTabLinks.forEach(function (tabLink) {
    tabLink.addEventListener('click', function (event) {
        event.preventDefault();
        activateResponseTab(tabLink.getAttribute('href'));
    });
});

var sivaDropzone = new Dropzone('#siva-dropzone');
sivaDropzone.on('complete', function () {
    sivaDropzone.removeAllFiles();
});

sivaDropzone.on('sending', function (file, xhr, formData) {
    resultArea.classList.add('hide');
    validationSummary.classList.add('hide');
    validationWarnings.classList.add('hide');
    jsonDataFilesLink.classList.add('hide');
    errorArea.classList.add('hide');
    validationWarningRows.textContent = '';
    jsonDataFilesReport.textContent = '';
    requestError.textContent = '';

    var policy = policySelect.value;
    var report = reportSelect.value;
    var type = typeSelect.value;
    var returnDataFiles = returnDataFilesCheckbox.checked;

    console.log('Validation policy: ' + policy);
    console.log('Report type: ' + report);
    formData.append('policy', policy);
    formData.append('type', type);
    formData.append('report', report);
    formData.append('encodedFilename', encodeURI(file.name));
    formData.append('returnDataFiles', returnDataFiles);
});

sivaDropzone.on('uploadprogress', function (file, progress) {
    document.getElementById('file-progress').value = progress;
});

sivaDropzone.on('success', function (file, response) {
    resultArea.classList.remove('hide');
    activateResponseTab('#json');

    validationReport.classList.add('hljs', 'language-json');
    validationReport.innerHTML = hljs.highlight(response.jsonValidationResult, { language: 'json' }).value;

    if (!isEmpty(response.jsonDataFilesResult)) {
        jsonDataFilesLink.classList.remove('hide');
        jsonDataFilesReport.classList.add('hljs', 'language-json');
        jsonDataFilesReport.innerHTML = hljs.highlight(response.jsonDataFilesResult, { language: 'json' }).value;
    }

    console.log(response);
    if (response.filename !== '') {
        validationSummary.classList.remove('hide');
        documentName.textContent = response.filename;
        overallValidationResult.classList.remove('invalid');
        overallValidationResult.classList.remove('valid');
        overallValidationResult.classList.add(response.overAllValidationResult.toLowerCase());
        overallValidationResult.textContent = response.overAllValidationResult;

        if (!isEmpty(response.validationWarnings)) {
            validationWarnings.classList.remove('hide');
            response.validationWarnings.forEach(function (value) {
                var listItem = document.createElement('li');
                listItem.textContent = value;
                validationWarningRows.appendChild(listItem);
            });
        }
    }
});

sivaDropzone.on('error', function (file, response) {
    errorArea.classList.remove('hide');
    requestError.textContent = JSON.stringify(response);
});

closeNotificationButton.addEventListener('click', function (e) {
    e.preventDefault();
    notification.classList.add('hide');
    setNotificationState('closed');
    clearLegacyNotificationState();
});
