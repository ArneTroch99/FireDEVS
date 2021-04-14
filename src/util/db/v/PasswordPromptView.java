package util.db.v;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;

import util.db.GenericPasswordProtectedDatabaseConnectionConfiguration;
import util.db.c.PasswordPromptController;

public class PasswordPromptView extends JDialog
{
    private JPasswordField passwordField;
    private JButton okButton, cancelButton;
    private PasswordPromptController controller;
    private GenericPasswordProtectedDatabaseConnectionConfiguration config;
    private boolean isCancelled;
    public static final String DIALOG_NAME = "Database Password Dialog";

    public PasswordPromptView(
        GenericPasswordProtectedDatabaseConnectionConfiguration config
    )
    {
        setTitle("Enter Database Password");
        setName(DIALOG_NAME);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());

        this.isCancelled = false;

        this.config = config;
        controller = new PasswordPromptController(this);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        passwordField.setName("Password_Field");
        passwordField.setColumns(10);

        okButton = new JButton("OK");
        okButton.addActionListener(controller.onOkClicked);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(controller.onCancelClicked);

        add(
            passwordLabel,
            addVerticalInsets(makeConstraint(0, 0))
        );
        add(
            passwordField,
            addVerticalInsets(
                addHorizontalInsets(doubleWide(rightJustify(makeConstraint(1, 0))), 10)
            )
        );

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(okButton, rightJustify(makeConstraint(0, 0)));
        buttonPanel.add(cancelButton, addHorizontalInsets(rightJustify(makeConstraint(1, 0)), 5));

        add(
            buttonPanel,
            addVerticalInsets(rightJustify(makeConstraint(2, 1)))
        );
        
        getRootPane().setDefaultButton(okButton);

        setSize(400, 200);
        pack();
        setResizable(false);
    }

    private GridBagConstraints makeConstraint(int x, int y)
    {
        GridBagConstraints s = new GridBagConstraints();
        s.anchor = GridBagConstraints.LINE_START;
        s.fill = GridBagConstraints.HORIZONTAL;
        s.gridx = x;
        s.gridy = y;

        return s;
    }

    private GridBagConstraints addHorizontalInsets(
        GridBagConstraints c,
        int width
    )
    {
        if (c.insets != null)
        {
            c.insets.left += width;
        }
        else
        {
            c.insets = new Insets(0, 10, 0, 0);
        }
        return c;
    }

    private GridBagConstraints addVerticalInsets(GridBagConstraints c)
    {
        if (c.insets != null)
        {
            c.insets.top += 40;
        }
        else
        {
            c.insets = new Insets(40, 0, 0, 0);
        }
        return c;
    }

    private GridBagConstraints doubleWide(GridBagConstraints c)
    {
        c.gridwidth += 1;
        return c;
    }

    private GridBagConstraints rightJustify(GridBagConstraints c)
    {
        c.anchor = GridBagConstraints.LINE_END;
        return c;
    }

    public GenericPasswordProtectedDatabaseConnectionConfiguration getConfig()
    {
        return this.config;
    }

    public String getPassword()
    {
        return new String(this.passwordField.getPassword());
    }

    public void closePrompt()
    {
        this.setVisible(false);
    }

    public void setCancelled()
    {
        isCancelled = true;
    }

    public boolean isCancelled()
    {
        return isCancelled;
    }
}
