package unit.utils

/**
 * Created by Emil Matevosyan
 * Date: 11/30/15.
 */
class MailVerifier {
    String[] to
    String from, subject, html, body
    boolean async, multipart
    def attachBytes

    void to(String[] to) { this.to = to }

    void from(String from) { this.from = from }

    void subject(String subject) { this.subject = subject }

    void html(String html) { this.html = html }

    void async(boolean async) { this.async = async }

    void multipart(boolean multipart) { this.multipart = multipart }

    void body(String body) { this.body = body }

    void attachBytes(def attachBytes) { this.attachBytes = attachBytes }
}
