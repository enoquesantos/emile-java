package gsort.pos.engsisubiq.EmileMobile

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class LostPasswordFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lost_password_main, container, false)
    }

    // override fun onCreate(savedInstanceState: Bundle?) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // add handle for buttons to the page
        super.onViewCreated(view, savedInstanceState)
    }
}

