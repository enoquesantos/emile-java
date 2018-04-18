package gsort.pos.engsisubiq.EmileMobile

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class UserProfileFragment : Fragment() {

    private var localStorage: LocalStorage? = null
    private var activity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as? MainActivity
        localStorage = LocalStorage.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.user_profile_main, container, false)
    }

    override fun onResume() {
        super.onResume()
        activity!!.setToolBarTitle("Meu perfil")
        activity!!.setNavViewItemSelected(R.id.my_profile)
    }
}
