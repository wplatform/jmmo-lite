package com.github.azeroth.defines;

public enum AiReaction {
    ALERT,                               // pre-aggro (used in client packet handler)
    FRIENDLY,                               // (NOT used in client packet handler)
    HOSTILE,                               // sent on every attack, triggers aggro sound (used in client packet handler)
    AFRAID,                               // seen for polymorph (when AI not in control of self?) (NOT used in client packet handler)
    DESTROY                                  // used on object destroy (NOT used in client packet handler)
}
