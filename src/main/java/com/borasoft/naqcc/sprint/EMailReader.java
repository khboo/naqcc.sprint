package com.borasoft.naqcc.sprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

// TODO: To handle exception when an invalid log is encountered.

public final class EMailReader {
  
  private String subjectToSearch = "None";
  private String outputDir;
  private String baseFilename = "NAQCC_";
  
  private String protocol;
  private String host = null;
  private String user = null;
  private String password = null;
  private String mbox = null;
  private int port = -1;
  private boolean verbose = true;
  @SuppressWarnings("unused")
  private boolean debug = false;
  private boolean showStructure = false;
  private boolean saveAttachments = false;
  private int attnum = 1;
  
  private int msgnum = -1;
  private int optind;

  private PrintWriter partWriter;
   
  public EMailReader(String[] argv) {
    for (optind = 0; optind < argv.length; optind++) {
      if (argv[optind].equals("-T")) {
        protocol = argv[++optind];
      } else if (argv[optind].equals("-H")) {
        host = argv[++optind];
      } else if (argv[optind].equals("-U")) {
        user = argv[++optind];
      } else if (argv[optind].equals("-P")) {
        password = argv[++optind];
      } else if (argv[optind].equals("-D")) {
        debug = true;
      } else if (argv[optind].equals("-S")) {
        subjectToSearch = argv[++optind];
      } else if (argv[optind].equals("-O")) {
        outputDir = argv[++optind];
      } else if (argv[optind].equals("--")) {
        optind++;
        break;
      } else if (argv[optind].startsWith("-")) {
        System.out.println("Usage: EmailReader [-T protocol] [-H host] [-U user]");
        System.out.println("\t[-P password] [-S subject] [-O outputDir");
        System.exit(1);
      } else {
        break;
      }
    }    

    if (optind < argv.length) {
      msgnum = Integer.parseInt(argv[optind]);
    }
  }
  
  /*
   * An example argv[] : 
   * 
   * Brinkster
   * -D -T pop3s -H webmail.brinkster.com -U foo@borasoft.com -P Bar -O c:/temp2 -S "NAQCC Sprint Log"
   * 
   * Hotmail
   * -D -T pop3s -H pop3.live.com -U foo@hotmail.com -P Bar -O c:/temp2 -S "NAQCC Sprint Log"
   * POP3 server: pop3.live.com (port 995)
   * SMTP server: smtp.live.com (port 25)
   */
  // test only
  public static void main(String[] args) {
		EMailReader emailReader = new EMailReader(args);
    emailReader.dumpMail();
		System.exit(0);
	}
  
  private int numEMailProcessed=0;
  public int getNumEMailProcessed() {
    return numEMailProcessed;
  }
  
  public void dumpMail() {
    try {
      // Get a Properties object
      Properties props = System.getProperties();

      // Get a Session object
      Session session = Session.getInstance(props, null);

      // Get a Store object
      Store store = null;
      if (protocol != null) {   
        store = session.getStore(protocol);
      } else {
        store = session.getStore();
      }
      // Connect
      if (host != null || user != null || password != null) {
        store.connect(host, port, user, password);
      } else {
        store.connect();
      }
      
      // Open the Folder
      Folder folder = store.getDefaultFolder();
      if (folder == null) {
        System.out.println("No default folder");
        System.exit(1);
      }

      if (mbox == null) {
        mbox = "INBOX";
        // Only INBOX supported
        //mbox = "NAQCC Sprints/mW 2013 Jun";
      }
      folder = folder.getFolder(mbox);
      if (folder == null) {
        System.out.println("Invalid folder");
        System.exit(1);
      }

      // try to open read/write and if that fails try read-only
      try {
        folder.open(Folder.READ_WRITE);
      } catch (MessagingException ex) {
        folder.open(Folder.READ_ONLY);
      }
      
      int totalMessages = folder.getMessageCount();
      if (totalMessages == 0) {
        System.out.println("Empty folder");
        folder.close(false);
        store.close();
        return;
      }

      if (verbose) {
        int newMessages = folder.getNewMessageCount();
        System.out.println("Total messages = " + totalMessages);
        System.out.println("New messages = " + newMessages);
        System.out.println("-------------------------------");
      }

      if (msgnum == -1) {
        // Attributes & Flags for all messages ..
        Message[] msgs = folder.getMessages();

        // Use a suitable FetchProfile
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        fp.add("X-Mailer");
        folder.fetch(msgs, fp);

        // This is where I need to process NAQCC log email.
        //
        File dir = new File(outputDir);
        if (!dir.exists()) {
          dir.mkdir();
        }
        File file = null;
        for (int i = 0; i < msgs.length; i++) {
          System.out.println("--------------------------");
          System.out.println("MESSAGE #" + (i + 1) + ":");
          dumpEnvelope(msgs[i]);
          if(msgs[i].getSubject().contains(subjectToSearch)) {
            file = new File(outputDir,baseFilename + i + ".txt");
            partWriter = new PrintWriter(file);
            dumpPart(msgs[i],partWriter);
            numEMailProcessed++;
            partWriter.close();
          }
        }
      } else {
        System.out.println("Getting message number: " + msgnum);
        Message m = null;
    
        try {
          m = folder.getMessage(msgnum);
          dumpPart(m);
          m.setFlag(Flag.DELETED,false);
        } catch (IndexOutOfBoundsException iex) {
          System.out.println("Message number out of range");
        }
      } // end else

      folder.close(false);
      store.close();
    } catch (Exception ex) { // top level try
      System.out.println("Oops, got exception! " + ex.getMessage());
      ex.printStackTrace();
      System.exit(1);
    }  
  }
  
  public void dumpPart(Part p) throws Exception {
    //String ct = p.getContentType();
    String filename = p.getFileName();
    if (filename != null) {
        pr("FILENAME: " + filename);
    }

    /*
     * Using isMimeType to determine the content type avoids
     * fetching the actual content data until we need it.
     */
    if (p.isMimeType("text/plain")) {
      pr("---------------------------");
      if (!showStructure && !saveAttachments) {
        System.out.println((String)p.getContent());
      } 
    } else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart)p.getContent();
      level++;
      int count = mp.getCount();
      for (int i = 0; i < count; i++) {
        dumpPart(mp.getBodyPart(i));
      }
      level--;
    } else if (p.isMimeType("message/rfc822")) {
      level++;
      dumpPart((Part)p.getContent());
      level--;
    } else {
      if (!showStructure && !saveAttachments) {
        // If we actually want to see the data, and it's not a
        // MIME type we know, fetch it and check its Java type.
        Object o = p.getContent();
        if (o instanceof String) {
          // do nothing
        } else if (o instanceof InputStream) {
          pr("This is just an input stream");
          pr("---------------------------");
          InputStream is = (InputStream)o;
          int c;
          while ((c = is.read()) != -1);
          System.out.write(c); // why do we need this line?
        } else {
          pr("This is an unknown type");
          pr("---------------------------");
          pr(o.toString());
        }
      } else {
        // just a separator
        pr("---------------------------");
      } 
    } // end else

    /*
     * If we're saving attachments, write out anything that
     * looks like an attachment into an appropriately named
     * file.  Don't overwrite existing files to prevent
     * mistakes.
     */
    if (saveAttachments && level != 0 && !p.isMimeType("multipart/*")) {
      String disp = p.getDisposition();
      // many mailers don't include a Content-Disposition
      if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
        if (filename == null) {
          filename = "Attachment" + attnum++;
        }
        pr("Saving attachment to file " + filename);
        try {
          File f = new File(filename);
          if (f.exists()) {
            // XXX - could try a series of names
            throw new IOException("file exists");
          }
          ((MimeBodyPart)p).saveFile(f);
        } catch (IOException ex) {
          pr("Failed to save attachment: " + ex);
        }
        pr("---------------------------");
      }
    }
  }
  
  public void dumpPart(Part p, PrintWriter writer) throws Exception {
    //String ct = p.getContentType();
    String filename = p.getFileName();
    if (filename != null) {
        pr("FILENAME: " + filename,writer);
    }

    /*
     * Using isMimeType to determine the content type avoids
     * fetching the actual content data until we need it.
     */
    if (p.isMimeType("text/plain")) {
      //pr("---------------------------",writer);
      if (!showStructure && !saveAttachments) {
        pr((String)p.getContent(),writer);
      } 
    } else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart)p.getContent();
      level++;
      int count = mp.getCount();
      for (int i = 0; i < count; i++) {
        dumpPart(mp.getBodyPart(i),writer);
      }
      level--;
    } else if (p.isMimeType("message/rfc822")) {
      level++;
      dumpPart((Part)p.getContent(),writer);
      level--;
    } else {
      if (!showStructure && !saveAttachments) {
        // If we actually want to see the data, and it's not a
        // MIME type we know, fetch it and check its Java type.
        Object o = p.getContent();
        if (o instanceof String) {
          // do nothing
        } else if (o instanceof InputStream) {
          pr("This is just an input stream",writer);
          pr("---------------------------",writer);
          InputStream is = (InputStream)o;
          int c;
          while ((c = is.read()) != -1);
          System.out.write(c); // why do we need this line?
        } else {
          pr("This is an unknown type",writer);
          pr("---------------------------",writer);
          pr(o.toString(),writer);
        }
      } else {
        // just a separator
        pr("---------------------------",writer);
      } 
    } // end else

    /*
     * If we're saving attachments, write out anything that
     * looks like an attachment into an appropriately named
     * file.  Don't overwrite existing files to prevent
     * mistakes.
     */
    if (saveAttachments && level != 0 && !p.isMimeType("multipart/*")) {
      String disp = p.getDisposition();
      // many mailers don't include a Content-Disposition
      if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
        if (filename == null) {
          filename = "Attachment" + attnum++;
        }
        pr("Saving attachment to file " + filename,writer);
        try {
          File f = new File(filename);
          if (f.exists()) {
            // XXX - could try a series of names
            throw new IOException("file exists");
          }
          ((MimeBodyPart)p).saveFile(f);
        } catch (IOException ex) {
          pr("Failed to save attachment: " + ex,writer);
        }
        pr("---------------------------",writer);
      }
    }
  }

  public void dumpEnvelope(Message m) throws Exception {
    //pr("This is the message envelope");
    pr("---------------------------");
    Address[] a;
    // FROM 
    if ((a = m.getFrom()) != null) {
      for (int j = 0; j < a.length; j++)
      pr("FROM: " + a[j].toString());
    }

    // REPLY TO
    if ((a = m.getReplyTo()) != null) {
      for (int j = 0; j < a.length; j++)
      pr("REPLY TO: " + a[j].toString());
    }

    // TO
    if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
      for (int j = 0; j < a.length; j++) {
        pr("TO: " + a[j].toString());
        InternetAddress ia = (InternetAddress)a[j];
        if (ia.isGroup()) {
          InternetAddress[] aa = ia.getGroup(false);
          for (int k = 0; k < aa.length; k++) {
            pr("  GROUP: " + aa[k].toString());
          }
        }
      } // end for
    } // end if

    // SUBJECT
    pr("SUBJECT: " + m.getSubject());

    // DATE
    Date d = m.getSentDate();
    pr("SendDate: " + (d != null ? d.toString() : "UNKNOWN"));

    // FLAGS
    Flags flags = m.getFlags();
    StringBuffer sb = new StringBuffer();
    Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

    boolean first = true;
    for (int i = 0; i < sf.length; i++) {
      String s;
      Flags.Flag f = sf[i];
      if (f == Flags.Flag.ANSWERED) {
        s = "\\Answered";
      } else if (f == Flags.Flag.DELETED) {
        s = "\\Deleted";
      } else if (f == Flags.Flag.DRAFT) {
        s = "\\Draft";
      } else if (f == Flags.Flag.FLAGGED) {
        s = "\\Flagged";
      } else if (f == Flags.Flag.RECENT) {
        s = "\\Recent";
      } else if (f == Flags.Flag.SEEN) {
        s = "\\Seen";
      } else {
        continue;   // skip it
      }
      if (first) {
        first = false;
      } else {
        sb.append(' ');
      }
      sb.append(s);
    }

    String[] uf = flags.getUserFlags(); // get the user flag strings
    for (int i = 0; i < uf.length; i++) {
      if (first) {
        first = false;
      } else {
        sb.append(' ');
      }
      sb.append(uf[i]);
    }
    pr("FLAGS: " + sb.toString());

    // X-MAILER
    String[] hdrs = m.getHeader("X-Mailer");
    if (hdrs != null) {
      pr("X-Mailer: " + hdrs[0]);
    } else {
      pr("X-Mailer NOT available");
    }
  }
  
  public void dumpEnvelope(Message m, PrintWriter writer) throws Exception {
    //pr("This is the message envelope");
    pr("---------------------------");
    Address[] a;
    // FROM 
    if ((a = m.getFrom()) != null) {
      for (int j = 0; j < a.length; j++)
      pr("FROM: " + a[j].toString());
    }

    // REPLY TO
    if ((a = m.getReplyTo()) != null) {
      for (int j = 0; j < a.length; j++)
      pr("REPLY TO: " + a[j].toString());
    }

    // TO
    if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
      for (int j = 0; j < a.length; j++) {
        pr("TO: " + a[j].toString());
        InternetAddress ia = (InternetAddress)a[j];
        if (ia.isGroup()) {
          InternetAddress[] aa = ia.getGroup(false);
          for (int k = 0; k < aa.length; k++) {
            pr("  GROUP: " + aa[k].toString());
          }
        }
      } // end for
    } // end if

    // SUBJECT
    pr("SUBJECT: " + m.getSubject());

    // DATE
    Date d = m.getSentDate();
    pr("SendDate: " + (d != null ? d.toString() : "UNKNOWN"));

    // FLAGS
    Flags flags = m.getFlags();
    StringBuffer sb = new StringBuffer();
    Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

    boolean first = true;
    for (int i = 0; i < sf.length; i++) {
      String s;
      Flags.Flag f = sf[i];
      if (f == Flags.Flag.ANSWERED) {
        s = "\\Answered";
      } else if (f == Flags.Flag.DELETED) {
        s = "\\Deleted";
      } else if (f == Flags.Flag.DRAFT) {
        s = "\\Draft";
      } else if (f == Flags.Flag.FLAGGED) {
        s = "\\Flagged";
      } else if (f == Flags.Flag.RECENT) {
        s = "\\Recent";
      } else if (f == Flags.Flag.SEEN) {
        s = "\\Seen";
      } else {
        continue;   // skip it
      }
      if (first) {
        first = false;
      } else {
        sb.append(' ');
      }
      sb.append(s);
    }

    String[] uf = flags.getUserFlags(); // get the user flag strings
    for (int i = 0; i < uf.length; i++) {
      if (first) {
        first = false;
      } else {
        sb.append(' ');
      }
      sb.append(uf[i]);
    }
    pr("FLAGS: " + sb.toString());

    // X-MAILER
    String[] hdrs = m.getHeader("X-Mailer");
    if (hdrs != null) {
      pr("X-Mailer: " + hdrs[0]);
    } else {
      pr("X-Mailer NOT available");
    }
  }

  private String indentStr = "                                               ";
  private int level = 0;

  /**
   * Print a, possibly indented, string.
   */
  public void pr(String s) {
    if (showStructure) {
      System.out.print(indentStr.substring(0, level * 2));
    }
    System.out.println(s);
  }

  public void pr(String s, PrintWriter writer) {
    if (showStructure) {
      writer.print(indentStr.substring(0, level * 2));
    }
    writer.println(s);
  }
  
  public String getOutputDir() {
    return outputDir;
  }
}
