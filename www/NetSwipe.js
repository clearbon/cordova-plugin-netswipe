var CardScanner = function () {
}

CardScanner.prototype = {
    /**
     * Init the API with the SDK params (provided by Jumio).
     *
     * @param {string} apiToken
     *      The API token
     * @param {string} apiSecret
     *      The API secret
     */
    init: function (apiToken, apiSecret) {
        cordova.exec(function (res) { console.log('CardScanner.init: success: ' + res) },
                     function (err) { console.log('CardScanner.init: error: ' + err) },
                     "NetSwipePlugin",
                     "init",
                     [apiToken, apiSecret]);
    },

    /** 
     * Start the scanner
     *
     * @param {Object} options
     *      The options to configure the scan
     * @param {Function} successCallback
     *      The callback function with the scan result object as parameter:
     *      {cardNumber, expiryMonth, expiryYear, cvv, cardHolderName, sortCode, accountNumber, cardNumberManuallyEntered}
     * @param {Function} errorCallback
     *      The callback in case of error with the error as parameter:
     *      {code, message}
     */
    scanCard: function (options, succesCallback, errorCallback) {
        cordova.exec(succesCallback,
                     errorCallback,
                     "NetSwipePlugin",
                     "scanCard",
                     [options]);
    }
}

var plugin = new CardScanner();
module.exports = plugin;
