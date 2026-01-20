package org.Blackjack.infrastructure;

public class ActorContext {
    private final ActorSystem system;
    private final ActorCell self;

    ActorContext(ActorSystem system, ActorCell self) {
        this.system = system;
        this.self = self;
    }

    public ActorRef fork(AbstractActor childActor) {
        return system.fork(childActor, self);
    }
    public void sendMessageToParent(Object msg) {
        self.parent().enqueue(new AsyncCommand<>(msg));
    }
}
