package util.db;

public class PasswordDialogCancelledException extends ConnectionTestException
{
    private static final long serialVersionUID = 1045717644533791840L;

    public PasswordDialogCancelledException()
    {
        super("No password entered for database connection!");
    }
}
