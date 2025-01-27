package org.example.listeners;

import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventListener extends ListenerAdapter
{
    private static final int joinLimit = 10;
    private final AtomicInteger joinCount = new AtomicInteger(0);
    private boolean invitesDisabled = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long TIME_FRAME_SECONDS = 3600; // Set the time frame for enabling invites
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)
    {
        User user = event.getUser(); //RETRIEVES USER DATA
        String emoji = event.getReaction().getEmoji().getAsReactionCode(); //RETRIEVES EMOJI USED
        String channelReference = event.getChannel().getId(); // RETRIEVES CHANNEL REFERENCED
        if (channelReference.equals("1128834160790868072") && emoji.equals("\u2705"))
        {
            Role roleWanderer = event.getGuild().getRoleById(1227692003131789343L);
            if (roleWanderer != null)
            {
                event.getGuild().addRoleToMember(user, roleWanderer).queue();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        User user = event.getAuthor();
        
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event)
    {
        String userTag = event.getMember().getUser().getAsTag();
        String logChannel = "1327454722348552294";
        LocalDateTime time = LocalDateTime.now();
        int currentUserCount = joinCount.incrementAndGet();
        String logMessage = "[ " + userTag + " ENTERED THE SERVER AT " + time + " ] ";
        event.getGuild().getTextChannelById(logChannel).sendMessage(logMessage).queue();
        if (currentUserCount > joinLimit)
        {
            disableInvites(event);
            if (logChannel != null)
            {
                event.getGuild().getTextChannelById(logChannel).sendMessage("[ALERT] - TOO MANY USERS HAVE JOINED CONCURRENTLY. INVITES HAVE BEEN DISABLED TEMPORARILY.").queue();
            }
            scheduler.schedule(() -> enableInvites(event), TIME_FRAME_SECONDS, TimeUnit.SECONDS);

        }
    }

    private void disableInvites(GuildMemberJoinEvent event)
    {
        event.getGuild().retrieveInvites().queue(invites ->
        {
            for (Invite invite : invites) {
                invite.delete().queue();
            }

        });
        invitesDisabled = true;

    ;}
    private void enableInvites(GuildMemberJoinEvent event)
    {
        invitesDisabled = false;
        String logChannel = "1327454722348552294";
        if (logChannel != null)
        {
            event.getGuild().getTextChannelById(logChannel).sendMessage("[ALERT] INVITES HAVE BEEN RE-ENABLED.").queue();
        }

    }

}