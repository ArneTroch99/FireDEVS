package util.db;

import util.db.v.PasswordPromptView;

public abstract class GenericPasswordProtectedDatabaseConnectionConfiguration implements DatabaseConnectionConfiguration
{
    protected String password;

    public void promptPassword() throws PasswordDialogCancelledException
    {
        PasswordPromptView passwordPrompt = new PasswordPromptView(this);
        passwordPrompt.setVisible(true);

        if (passwordPrompt.isCancelled())
        {
            throw new PasswordDialogCancelledException();
        }
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
}
