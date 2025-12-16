package org.Blackjack.infrastructure;

public interface Actor<C, R> {
    R onReceive(C command);
    default ActorRouter router() {
      return ActorRouter.instance();
    };

}
