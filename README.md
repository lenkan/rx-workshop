# rx-workshop

Hello!
Welcome to the workshop. I hope that you will enjoy it. Please read this readme to save yourself some time :)

##First step
Clone this repo!

##Getting started

###Starting the UI
Tested with `Chromium 47.0.2526.73 Ubuntu 15.10 (64-bit)`.

In order to start the UI, open [ws-ui/index.html](ws-ui/index.html) (locally) in Chrome (or another browser if you dare :) ). 
The UI will try to establish a connection to a backend on page load. It assumes the address `ws://localhost:4739`.
Check the console for errors (Ctrl+Shift+J in Chrome), and reload the page if you restart the server.

###Starting a backend

You can choose one of the existing backends, or write your own:

####Starting the Java backend
Tested with `OpenJDK 1.8.0_66-internal` and `Maven 3.2.5`.

`cd` into [ws-java](ws-java/) and run `mvn exec:java` to start the server. Refresh the UI page and type into the text box. You should
see the app logging events to stdout:

```
Server started
connect from /0:0:0:0:0:0:0:1:50200
message from /0:0:0:0:0:0:0:1:50200: {"type":"query.input","text":"a"}
message from /0:0:0:0:0:0:0:1:50200: {"type":"query.input","text":"ap"}
message from /0:0:0:0:0:0:0:1:50200: {"type":"query.input","text":"apa"}
```

You can also just open the maven project in your favourite editor and run `Server.main`.

Start editing in Handler.java to implement the necessary functionality.

**Note**: there are some util methods in `Util.java` for doing URLencoding and JSON conversion. Save time by using it!

####Starting the Node backend
Tested with `Node.JS 5.5.0`.

`cd` into [ws-node](ws-node/) and run `npm install` followed by `node index.js`. Refresh the UI page and type into the text box.
You should see the app logging events to stdout:

```
connection from file://
msg: {"type":"utf8","utf8Data":"{\"type\":\"query.input\",\"text\":\"a\"}"}
msg: {"type":"utf8","utf8Data":"{\"type\":\"query.input\",\"text\":\"ap\"}"}
msg: {"type":"utf8","utf8Data":"{\"type\":\"query.input\",\"text\":\"apa\"}"}
```

Open `index.js` in your favourite editor (not Emacs ;P ) and edit the `Handler` function to implement the functionality.

####Making your own backend
Make a server which accepts WS connections on ws://localhost:4739. Accept text messages like these:
```
{"type":"query.input","text":"a"}
{"type":"enter.press"}
{"type":"go.click"}
{"type":"instant.enable","value":true}
```

and answer with messages like these:
```
{"links":["http://a.com","http://b.com"]}
```

Convert incoming messages to `Observable`s: one type of `Observable` per type of incoming message and one type of `Observable`
for outgoing messages. Implement the functionality by building rx pipelines with the incoming `Observable`s and pushing the result to
the outgoing `Observable`. A simple way of bridging is to use a [Subject](http://reactivex.io/documentation/subject.html). 

Also add a HTTP client to use with Rx on the backend.

See the existing impls for inspiration.

**Note**: Don't forget that you will get multiple clients if you reload the page!

##The assignments

##The assignments

1. Make sure that the client result list gets populated with a single URL on connect. This can be any hardcoded URL such as `http://www.welcometomybackend.com` or similar. Push a "links" message with this URL as soon as the client connects. Hint: the `Handler` class is created when a new client is connected.
2. Do an actual search when the client presses the "go" button. Use the `goClicks` and `queryInput` observables to build a pipeline which performs a http request to the DuckDuckGo API which the entered search term. The API is specified at [https://duckduckgo.com/api](https://duckduckgo.com/api). Filter out the `RelatedTopic`s which have `FirstURL`s
3. Expand 2 to also trigger when the user presses the enter key in the search field.

