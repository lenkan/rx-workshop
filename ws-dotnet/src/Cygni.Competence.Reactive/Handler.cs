using System;
using System.Reactive.Subjects;

namespace Cygni.Competence.Reactive
{
    public class Handler
    {
        public Handler(IObserver<object> messages)
        {
            Messages = messages;
            Messages.OnNext(new { links = new[] { "http://hej" } });
            QueryInputs = new Subject<string>();
            GoClicks = new Subject<string>();
            InstantSearchChanges = new Subject<bool>();
            EnterPresses = new Subject<string>();
        }

        public Subject<string> EnterPresses { get; }

        public Subject<bool> InstantSearchChanges { get; }

        public IObserver<object> Messages { get; }

        public Subject<string> GoClicks { get; }

        public Subject<string> QueryInputs { get; }
    }
}