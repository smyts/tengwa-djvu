package tengwa.djvu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GoToDialog extends Dialog {
    interface GoToPageAction{
        void goToPage(int page);
    }

    private GoToPageAction mGoToPageAction = null;
    private String mHeader;

    public GoToDialog(Context context, String header){
        super(context);
        mHeader = header;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.go_to_dialog);
        setTitle(R.string.go_to);
        ((TextView) findViewById(R.id.go_to_page_header)).setText(mHeader);

        ViewGroup.LayoutParams params = getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        Button btn = (Button) findViewById(R.id.go_to_page_button);
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (mGoToPageAction != null){
                    int page;
                    try{
                        EditText ed = (EditText) findViewById(R.id.page_to_go);
                        page = Integer.parseInt(ed.getText().toString());
                    }
                    catch (NumberFormatException ex){
                        /*
                         * If we have problems with parsing page number
                         * we have to show to the parent activity
                         * that page number is wrong.
                         * Page "-1" is sure to be wrong.
                         */
                        page = -1;
                    }
                    mGoToPageAction.goToPage(page);
                }
                GoToDialog.this.cancel();
            }
        });
    }

    public void setGoToPageAction(GoToPageAction action){
        mGoToPageAction = action;
    }
}
