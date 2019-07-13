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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PushController {

    private static final String COMMAND_NODE_REGISTER_FCM = "register-push-fcm";
    private static final String COMMAND_NODE_UNREGISTER_FCM = "unregister-push-fcm";

    public static IQHandler commandHandler = (iq -> {
        final Command command = iq.getExtension(Command.class);
        if (command != null && command.getAction() == Command.Action.EXECUTE) {
            if (COMMAND_NODE_REGISTER_FCM.equals(command.getNode())) {
                return register(iq, command);
            } else if (COMMAND_NODE_UNREGISTER_FCM.equals(command.getNode())) {
                return unregister(iq, command);
            }
        }
        return iq.createError(Condition.BAD_REQUEST);
    });
    public static IQHandler pubsubHandler = (iq -> {
        PubSub pubSub = iq.getExtension(PubSub.class);
        if (pubSub != null && iq.getType() == IQ.Type.SET) {
            final String node = pubSub.getPublish() != null ? pubSub.getPublish().getNode() : null;
            final Jid jid = iq.getFrom();
            final String secret = pubSub.getPublishOptions() != null ? pubSub.getPublishOptions().findValue("secret") : null;

            if (node != null && secret != null && jid.isBareJid()) {
                final Jid domain = Jid.ofDomain(jid.getDomain());
                final Target target = TargetStore.getInstance().find(domain, node);
                if (target != null) {
                    if (secret.equals(target.getSecret())) {
                        final Message message = Message.createHighPriority(target, Configuration.getInstance().isCollapse());
                        if (FcmService.getInstance().push(message)) {
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

    private static IQ register(final IQ iq, final Command command) {
        final Optional<DataForm> optionalData = command.getPayloads().stream()
                .filter(p -> p instanceof DataForm)
                .map(p -> (DataForm) p)
                .findFirst();
        final Jid from = iq.getFrom().asBareJid();
        if (optionalData.isPresent()) {
            final DataForm data = optionalData.get();
            final String androidId = data.findValue("android-id");
            final String token = data.findValue("token");
            final Jid muc = data.findValueAsJid("muc");

            if (isNullOrEmpty(token) || isNullOrEmpty(androidId)) {
                return iq.createError(Condition.BAD_REQUEST);
            }

            if (muc != null && muc.isFullJid()) {
                return iq.createError(Condition.BAD_REQUEST);
            }

            final String device = Utils.combineAndHash(from.toEscapedString(), androidId);
            final String channel = muc == null ? "" : Utils.combineAndHash(muc.toEscapedString(), androidId);

            Target target = TargetStore.getInstance().find(device, channel);

            if (target != null) {
                if (target.setToken(token)) {
                    if (!TargetStore.getInstance().update(target)) {
                        return iq.createError(Condition.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                if (muc == null) {
                    target = Target.create(from, androidId, token);
                } else {
                    target = Target.createMuc(from, muc, androidId, token);
                }
                TargetStore.getInstance().create(target);
            }
            final Command result = new Command(command.getNode(),
                    String.valueOf(System.currentTimeMillis()),
                    Command.Action.COMPLETE,
                    Collections.singletonList(createRegistryResponseDataForm(target.getNode(), target.getSecret()))
            );
            return iq.createResult(result);
        } else {
            return iq.createError(Condition.BAD_REQUEST);
        }
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static DataForm createRegistryResponseDataForm(String node, String secret) {
        List<DataForm.Field> fields = new ArrayList<>();
        fields.add(DataForm.Field.builder().var("jid").value(Configuration.getInstance().getJid()).build());
        fields.add(DataForm.Field.builder().var("node").value(node).build());
        fields.add(DataForm.Field.builder().var("secret").value(secret).build());
        return new DataForm(DataForm.Type.FORM, fields);
    }

    private static IQ unregister(final IQ iq, final Command command) {
        final Optional<DataForm> optionalData = command.getPayloads().stream()
                .filter(p -> p instanceof DataForm)
                .map(p -> (DataForm) p)
                .findFirst();
        final Jid from = iq.getFrom().asBareJid();
        if (optionalData.isPresent()) {
            final DataForm data = optionalData.get();
            final String androidId = data.findValue("android-id");
            final String channel = data.findValue("channel");
            if (isNullOrEmpty(channel) || isNullOrEmpty(androidId)) {
                return iq.createError(Condition.BAD_REQUEST);
            }
            final String device = Utils.combineAndHash(from.toEscapedString(), androidId);
            if (TargetStore.getInstance().delete(device, channel)) {
                final Command result = new Command(command.getNode(), Command.Action.COMPLETE);
                return iq.createResult(result);
            } else {
                return iq.createError(Condition.ITEM_NOT_FOUND);
            }
        } else {
            return iq.createError(Condition.BAD_REQUEST);
        }
    }

}
