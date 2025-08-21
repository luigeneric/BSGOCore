package io.github.luigeneric.core.sector.management;

import io.github.luigeneric.core.User;

import java.util.Arrays;

public record NewUserJumpIn(User user, long[] groupJumpPlayerIds)
{
    @Override
    public String toString()
    {
        return "NewUserJumpIn{" +
                "user=" + user.getPlayer().getUserID() +
                ", groupJumpPlayerIds=" + Arrays.toString(groupJumpPlayerIds) +
                '}';
    }

    public boolean isGroupJump()
    {
        return groupJumpPlayerIds != null && groupJumpPlayerIds.length > 0;
    }
}
