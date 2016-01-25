var WebSocketServer = require('websocket').server;
var http = require('http');
var rx = require('rx');
var rp = require('request-promise');
var qs = require('querystring');

function Handler(messages) {
  this.messages = messages;
  this.goClicks = new rx.Subject();
  this.queryInputs = new rx.Subject();
  this.instantSearchChanges = new rx.Subject();
  this.enterPresses = new rx.Subject();
}

var server = http.createServer(function(req, resp) {

});
server.listen(4739, function() {});

wsServer = new WebSocketServer({ httpServer: server });
wsServer.on('request', function(request) {
  console.log('connection from ' + request.origin);
  var connection = request.accept(null, request.origin);

  var messages = new rx.Subject();
  messages.subscribe(function(o) {
    var txt = JSON.stringify(o);
    console.log("sending '" + txt + "'");
    connection.sendUTF(txt);
  });
  var handler = new Handler(messages);
  connection.on('message', function(wsMsg) {
    console.log('msg: ' + JSON.stringify(wsMsg));
    if (wsMsg.type !== 'utf8') {
      return;
    }
    var textMessage = wsMsg.utf8Data;
    var message = JSON.parse(textMessage);
    if (message.type === "go.click") {
      handler.goClicks.onNext("");
    }    
    else if (message.type === "query.input") {
      handler.queryInputs.onNext(message.text);
    } 
    else if (message.type == "instant.enable") {
      handler.instantSearchChanges.onNext(message.value);
    }
    else if (message.type == "enter.press") {
      handler.enterPresses.onNext("");
    }
  });

  connection.on('close', function(connection) {
  });
});
