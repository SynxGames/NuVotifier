package com.vexsoftware.votifier.fabric.event;

import com.vexsoftware.votifier.model.Vote;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface VotifierEvent {

    Event<VotifierEvent> EVENT = EventFactory.createArrayBacked(VotifierEvent.class,
            (listeners) -> (vote) -> {
                for (VotifierEvent listener : listeners) {
                    listener.interact(vote);
                }
            }
    );

    void interact(Vote vote);

}
