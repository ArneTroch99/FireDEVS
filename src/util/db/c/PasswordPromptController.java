package util.db.c;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import util.db.v.PasswordPromptView;

public class PasswordPromptController
{
    private PasswordPromptView view;

    public PasswordPromptController(PasswordPromptView view)
    {
        this.view = view;
    }
    
    public final ActionListener onOkClicked = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            view.getConfig().setPassword(view.getPassword());
            view.closePrompt();
        }
        
    };
    
    public final ActionListener onCancelClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            view.setCancelled();
            view.closePrompt();
        }
    };
}
