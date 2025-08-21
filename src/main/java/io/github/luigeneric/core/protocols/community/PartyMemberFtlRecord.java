package io.github.luigeneric.core.protocols.community;


import io.github.luigeneric.core.User;
import io.github.luigeneric.enums.PartyMemberFtlState;

public record PartyMemberFtlRecord(User user, PartyMemberFtlState state)
{
}
