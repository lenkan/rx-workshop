using System;
using System.Collections.Generic;
using System.Reactive.Subjects;
using Fleck;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Cygni.Competence.Reactive
{
    public class Server
    {

        private readonly Dictionary<IWebSocketConnection, Handler> _map = new Dictionary<IWebSocketConnection, Handler>();

        public void Run()
        {

            var server = new WebSocketServer("ws://0.0.0.0:4739");
            server.Start(socket =>
            {
                socket.OnOpen = () =>
                {
                    Console.WriteLine("Open!");
                    var messages = new Subject<object>();
                    messages.Subscribe(s => socket.Send(JsonConvert.SerializeObject(s)));
                    var handler = new Handler(messages);
                    _map.Add(socket, handler);
                };
                socket.OnClose = () => Console.WriteLine("Close!");
                socket.OnMessage = s => OnMessage(s, socket);
            });
        }

        public static void Main(string[] args)
        {
            try
            {
                Console.WriteLine("I AM SERVER");

                var server =new Server();
                server.Run();

                Console.ReadLine();

            }
            catch (Exception ex)
            {

                Console.WriteLine(ex.ToString());
            }
        }



        private void OnMessage(string s, IWebSocketConnection socket)
        {
            var o = JsonConvert.DeserializeObject<JObject>(s);
            var handler = _map[socket];
            switch (o["type"].ToString())
            {
                case "go.click":
                    handler.GoClicks.OnNext("");
                    break;
                case "query.input":
                    handler.QueryInputs.OnNext((string)o["text"]);
                    break;
                case "instant.enable":
                    handler.InstantSearchChanges.OnNext((bool)o["value"]);
                    break;
                case "enter.press":
                    handler.EnterPresses.OnNext("");
                    break;
                default:
                    Console.WriteLine("WARN: Unknown message type '" + o["type"] + "': '" + s + "'");
                    break;
            }
        }
    }
}
