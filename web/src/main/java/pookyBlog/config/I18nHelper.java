package pookyBlog.config;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

public class I18nHelper implements Mustache.Lambda {

    private final MessageSource messageSource;
    private final Locale locale;

    public I18nHelper(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public void execute(Template.Fragment frag, Writer out) throws IOException {
        StringWriter writer = new StringWriter();
        frag.execute(writer);
        String key = writer.toString().trim();
        String message = messageSource.getMessage(key, null, key, locale);
        out.write(message);
    }
}
