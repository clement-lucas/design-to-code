/* Proman - Application JavaScript */

/**
 * Open client search popup window.
 */
function openClientSearch() {
    var url = '/client/search';
    var options = 'width=600,height=500,scrollbars=yes,resizable=yes';
    window.open(url, 'clientSearch', options);
}

/**
 * Callback from client search popup.
 * Sets the selected client ID and name on the parent form.
 */
function setClient(clientId, clientName) {
    var clientIdField = document.getElementById('clientId') ||
                        document.querySelector('input[name="clientId"]');
    var clientNameField = document.getElementById('clientName');

    if (clientIdField) {
        clientIdField.value = clientId;
    }
    if (clientNameField) {
        clientNameField.value = clientName;
    }
}

/**
 * Confirm before form submission for destructive actions.
 */
function confirmSubmit(message) {
    return confirm(message || '実行してもよろしいですか？');
}
