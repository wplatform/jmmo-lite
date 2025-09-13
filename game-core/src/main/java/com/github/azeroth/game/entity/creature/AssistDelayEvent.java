package com.github.azeroth.game.entity.creature;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.unit.Unit;

import java.util.ArrayList;

public class AssistDelayEvent extends BasicEvent {
    private final ArrayList<ObjectGuid> m_assistants = new ArrayList<>();
    private final Unit m_owner;


    private final ObjectGuid m_victim;

    private AssistDelayEvent() {
    }

    public AssistDelayEvent(ObjectGuid victim, Unit owner) {
        m_victim = victim;
        m_owner = owner;
    }


    @Override
    public boolean execute(long etime, int pTime) {
        var victim = global.getObjAccessor().GetUnit(m_owner, m_victim);

        if (victim != null) {
            while (!m_assistants.isEmpty()) {
                var assistant = m_owner.getMap().getCreature(m_assistants.get(0));
                m_assistants.remove(0);

                if (assistant != null && assistant.canAssistTo(m_owner, victim)) {
                    assistant.setNoCallAssistance(true);
                    assistant.engageWithTarget(victim);
                }
            }
        }

        return true;
    }

    public final void addAssistant(ObjectGuid guid) {
        m_assistants.add(guid);
    }
}
