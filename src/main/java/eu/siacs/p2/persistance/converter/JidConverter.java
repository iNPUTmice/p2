package eu.siacs.p2.persistance.converter;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import rocks.xmpp.addr.Jid;

public class JidConverter implements Converter<Jid> {

    @Override
    public Jid convert(Object value) throws ConverterException {
        if (value instanceof String) {
            return Jid.ofEscaped((String) value);
        } else if (value == null) {
            return null;
        }
        throw new ConverterException(
                String.format("Unable to convert from %s to Jid", value.getClass().getName()));
    }

    @Override
    public String toDatabaseParam(final Jid value) {
        return value.toEscapedString();
    }
}
