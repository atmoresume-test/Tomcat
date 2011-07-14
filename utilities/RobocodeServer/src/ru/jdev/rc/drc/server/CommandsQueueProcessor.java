/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

public class CommandsQueueProcessor implements Runnable {

    private final CommandsQueue commandsQueue;
    private final RCBattlesExecutor rcBattlesExecutor;

    private volatile boolean isRunned = true;

    public CommandsQueueProcessor(CommandsQueue commandsQueue,
                                  RCBattlesExecutor rcBattlesExecutor) {
        this.commandsQueue = commandsQueue;
        this.rcBattlesExecutor = rcBattlesExecutor;
    }

    public void run() {
        while (isRunned && !Thread.interrupted()) {
            try {
                final Command command = commandsQueue.getBattleRequest();
                if (!command.client.isAlive()) {
                    continue;
                }
                final BattleRequest battleRequest = command.battleRequest;
                final RSBattleResults rsBattleResults = rcBattlesExecutor.executeBattle(battleRequest.competitors, battleRequest.bfSpec, battleRequest.rounds);
                command.client.sendRSBattleResults(rsBattleResults);
            } catch (InterruptedException e) {
                isRunned = false;
            }
        }
    }

    public void stop() {
        isRunned = false;
    }

}
