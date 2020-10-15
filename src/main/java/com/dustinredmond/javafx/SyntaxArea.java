package com.dustinredmond.javafx;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.GridPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;

public class SyntaxArea extends SwingNode {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // settle for an ugly text editor
            // but systemLookAndFeel should always be present
        }
    }

    private final RSyntaxTextArea textArea;

    public void setText(String text) {
        textArea.setText(text);
    }

    public String getText() {
        return textArea.getText();
    }

    public void applyFormatting(String fileName) {
        textArea.setSyntaxEditingStyle(guessSyntaxFromFile(fileName));
    }

    public SyntaxArea(String fileName) {
        textArea = new RSyntaxTextArea(Integer.MAX_VALUE, 250);
        String syntaxType = this.guessSyntaxFromFile(fileName);
        textArea.setSyntaxEditingStyle(syntaxType);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        this.setContent(sp);
    }

    private String guessSyntaxFromFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty() || !fileName.contains(".")) {
            return SyntaxConstants.SYNTAX_STYLE_NONE;
        }

        String[] fileParts = fileName.toLowerCase().split("\\.");
        String ext = fileParts[fileParts.length - 1];

        switch (ext) {
            case "as": return SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT;
            case "c": return SyntaxConstants.SYNTAX_STYLE_C;
            case "clj" : return SyntaxConstants.SYNTAX_STYLE_CLOJURE;
            case "cpp" : return SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case "cs" : return SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case "css" : return SyntaxConstants.SYNTAX_STYLE_CSS;
            case "csv" : return SyntaxConstants.SYNTAX_STYLE_CSV;
            case "d" : return SyntaxConstants.SYNTAX_STYLE_D;
            case "dart" : return SyntaxConstants.SYNTAX_STYLE_DART;
            case "pas" :
            case "dfm" :
                return SyntaxConstants.SYNTAX_STYLE_DELPHI;
            case "dtd" : return SyntaxConstants.SYNTAX_STYLE_DTD;
            case "f" :
            case "f08" :
            case "f03" :
            case "f95" :
            case "f90" :
            case "ftn" :
            case "for" :
                return SyntaxConstants.SYNTAX_STYLE_FORTRAN;
            case "go" : return SyntaxConstants.SYNTAX_STYLE_GO;
            case "groovy" : return SyntaxConstants.SYNTAX_STYLE_GROOVY;
            case "htaccess" : return SyntaxConstants.SYNTAX_STYLE_HTACCESS;
            case "htm" :
            case "html" :
                return SyntaxConstants.SYNTAX_STYLE_HTML;
            case "ini" : return SyntaxConstants.SYNTAX_STYLE_INI;
            case "java" : return SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "js" : return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "json" : return SyntaxConstants.SYNTAX_STYLE_JSON;
            case "jsp" : return SyntaxConstants.SYNTAX_STYLE_JSP;
            case "latex" : return SyntaxConstants.SYNTAX_STYLE_LATEX;
            case "less" : return SyntaxConstants.SYNTAX_STYLE_LESS;
            case "lisp" : return SyntaxConstants.SYNTAX_STYLE_LISP;
            case "lua" : return SyntaxConstants.SYNTAX_STYLE_LUA;
            case "mxml" : return SyntaxConstants.SYNTAX_STYLE_MXML;
            case "nsis" : return SyntaxConstants.SYNTAX_STYLE_NSIS;
            case "pl" : return SyntaxConstants.SYNTAX_STYLE_PERL;
            case "php" : return SyntaxConstants.SYNTAX_STYLE_PHP;
            case "py" : return SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "rb" : return SyntaxConstants.SYNTAX_STYLE_RUBY;
            case "sas" : return SyntaxConstants.SYNTAX_STYLE_SAS;
            case "scala" :
            case "sc" :
                return SyntaxConstants.SYNTAX_STYLE_SCALA;
            case "sql" : return SyntaxConstants.SYNTAX_STYLE_SQL;
            case "properties" : return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            case "tcl" : return SyntaxConstants.SYNTAX_STYLE_TCL;
            case "ts" : return SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case "sh" : return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case "bat" : return SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case "xml" : return SyntaxConstants.SYNTAX_STYLE_XML;
            case "yaml" : return SyntaxConstants.SYNTAX_STYLE_YAML;
            default: return SyntaxConstants.SYNTAX_STYLE_NONE;
        }

    }
}
