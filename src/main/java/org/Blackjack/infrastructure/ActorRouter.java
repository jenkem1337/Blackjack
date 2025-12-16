package org.Blackjack.infrastructure;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ActorRouter {
    private static class InnerRouter {
        public static ActorRouter INSTANCE = new ActorRouter(new ConcurrentSkipListMap<>());
    }
    private final ConcurrentNavigableMap<String, ActorRef> router;

    private ActorRouter(ConcurrentNavigableMap<String, ActorRef> router) {
        this.router = router;
    }

    public static ActorRouter instance(){
        return InnerRouter.INSTANCE;
    }
    public CompletableFuture<Response> send(String actorRouterPath, Command command) {
        return router.get(actorRouterPath).send(command);
    }

    public void emit(String actorRouterPath, Command command){
        router.get(actorRouterPath).send(command);
    }
    public void broadcast(String subActorRouterPath, Command command) {
        router.subMap(subActorRouterPath, String.valueOf(Character.MAX_VALUE))
                .forEach((actorRouterPath, actorRef) -> actorRef.send(command));
    }
    public void registerActor(String actorRouterPath, ActorRef actorRef) {
        router.putIfAbsent(actorRouterPath, actorRef);
    }
    public ConcurrentNavigableMap<String, ActorRef> router() {
        return router;
    }
    public void clear() {
        router.clear();
    }
}
