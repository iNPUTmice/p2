package eu.siacs.p2.controller;

import eu.siacs.p2.Configuration;
import eu.siacs.p2.Utils;
import eu.siacs.p2.fcm.FcmService;
import eu.siacs.p2.fcm.Message;
import eu.siacs.p2.persistance.TargetStore;
import eu.siacs.p2.pojo.Target;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.commands.model.Command;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.pubsub.model.PubSub;

import java.util.*;

public class PushController {

    public static IQHandler register = (iq -> {
        final Command command = iq.getExtension(Command.class);
        if (command != null && command.getAction() == Command.Action.EXECUTE) {
            Optional<DataForm> data = command.getPayloads().stream()
                    .filter(p -> p instanceof DataForm)
                    .map(p -> (DataForm) p)
                    .findFirst();
            final Jid from = iq.getFrom().asBareJid();
            if (data.isPresent()) {
                final String androidId = data.get().findValue("android-id");
                final String token = data.get().findValue("token");

                if (token == null || androidId == null) {
                    return iq.createError(Condition.BAD_REQUEST);
                }

                final String device = Utils.combineAndHash(from.toEscapedString(), androidId);

                Target target = TargetStore.getInstance().find(device);

                if (target != null) {
                    if (target.setToken(token)) {
                        TargetStore.getInstance().update(target);
                    }
                    target.setToken(token);
                } else {
                    target = Target.create(from, androidId, token);
                    TargetStore.getInstance().create(target);
                }
                Command result = new Command(command.getNode(),
                        String.valueOf(System.currentTimeMillis()),
                        Command.Action.COMPLETE,
                        Collections.singletonList(createRegistryResponseDataForm(target.getNode(), target.getSecret()))
                );
                return iq.createResult(result);
            }
        }
        return iq.createError(Condition.BAD_REQUEST);
    });

    public static IQHandler push = (iq -> {
        PubSub pubSub = iq.getExtension(PubSub.class);
        if (pubSub != null && iq.getType() == IQ.Type.SET) {
            final String node = pubSub.getPublish() != null ? pubSub.getPublish().getNode() : null;
            final Jid jid = iq.getFrom();
            final String secret = pubSub.getPublishOptions() != null ? pubSub.getPublishOptions().findValue("secret") : null;

            if (node != null && secret != null  && jid.isBareJid()) {
                final Target target = TargetStore.getInstance().find(Jid.ofDomain(jid), node);
                if (target != null) {
                    if (secret.equals(target.getSecret())) {
                        if (FcmService.getInstance().push(Message.createHighPriority(target.getDevice(), target.getToken()))) {
                            return iq.createResult();
                        } else {
                            return iq.createError(Condition.RECIPIENT_UNAVAILABLE);
                        }

                    } else {
                        return iq.createError(Condition.FORBIDDEN);
                    }
                } else {
                    return iq.createError(Condition.ITEM_NOT_FOUND);
                }
            } else {
                return iq.createError(Condition.FORBIDDEN);
            }
        }
        return iq.createError(Condition.BAD_REQUEST);
    });

    private static DataForm createRegistryResponseDataForm(String node, String secret) {
        List<DataForm.Field> fields = new ArrayList<>();
        fields.add(DataForm.Field.builder().var("jid").value(Configuration.getInstance().getJid()).build());
        fields.add(DataForm.Field.builder().var("node").value(node).build());
        fields.add(DataForm.Field.builder().var("secret").value(secret).build());
        return new DataForm(DataForm.Type.FORM, fields);
    }

}
