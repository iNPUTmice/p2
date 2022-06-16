package eu.siacs.p2;

import com.google.common.base.Strings;
import eu.siacs.p2.controller.PushController;
import eu.siacs.p2.xmpp.extensions.push.Notification;
import java.security.SecureRandom;
import java.security.Security;
import org.apache.commons.cli.*;
import org.conscrypt.Conscrypt;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.extensions.commands.model.Command;
import rocks.xmpp.extensions.component.accept.ExternalComponent;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.muc.model.Muc;
import rocks.xmpp.extensions.pubsub.model.PubSub;

public class P2 {

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Options OPTIONS;
    private static final int RETRY_INTERVAL = 5000;

    private static Configuration configuration;

    static {
        OPTIONS = new Options();
        OPTIONS.addOption(new Option("c", "config", true, "Path to the config file"));
    }

    public static void main(final String... args) {
        try {
            main(new DefaultParser().parse(OPTIONS, args));
        } catch (final ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void main(final CommandLine commandLine) {
        final String config = commandLine.getOptionValue('c');
        if (Strings.isNullOrEmpty(config)) {
            configuration = EnvironmentConfiguration.create();
        } else {
            configuration = FileConfiguration.read(config);
        }

        configuration.validate();

        Security.insertProviderAt(Conscrypt.newProvider(), 1);

        final XmppSessionConfiguration.Builder builder = XmppSessionConfiguration.builder();

        builder.extensions(Extension.of(Notification.class));

        if (configuration.debug()) {
            builder.debugger(ConsoleDebugger.class);
        }

        final ExternalComponent externalComponent =
                ExternalComponent.create(
                        configuration.jid(),
                        configuration.sharedSecret(),
                        builder.build(),
                        configuration.host(),
                        configuration.port());

        externalComponent.addIQHandler(Command.class, PushController.commandHandler);
        externalComponent.addIQHandler(PubSub.class, PushController.pubsubHandler);

        externalComponent.getManager(ServiceDiscoveryManager.class).setEnabled(false);
        externalComponent.disableFeature(Muc.NAMESPACE);

        connectAndKeepRetrying(externalComponent);
    }

    private static void connectAndKeepRetrying(final ExternalComponent component) {
        while (true) {
            try {
                component.connect();
                while (component.isConnected()) {
                    Utils.sleep(500);
                }
            } catch (XmppException e) {
                System.err.println(e.getMessage());
            }
            Utils.sleep(RETRY_INTERVAL);
        }
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
}
