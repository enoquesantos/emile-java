package gsort.pos.engsisubiq.EmileMobile

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.*
import org.json.JSONObject
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import android.view.animation.AnimationUtils
import android.graphics.Point
import android.support.v7.widget.CardView
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager

class MessagesFragment : Fragment(), IRequestHttpFragment, AbsListView.OnScrollListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var isRequestRunning    : Boolean                = false
    private var listView            : ListView?              = null
    private var floatingButton      : FloatingActionButton?  = null
    private var activity            : MainActivity?          = null
    private var requestHandle       : MessagesRequestHandle? = null
    private var swipeRefreshWidget  : SwipeRefreshLayout?    = null
    private var listViewAdapter     : SimpleAdapter?         = null
    private var messagesOnTheView   : ArrayList<Int>?        = null
    private var userProfile         : UserProfile?           = null
    private var pageView            : View?                  = null
    private var listViewModel       : ArrayList<HashMap<String,String>>? = null
    private var fieldsNames         = arrayOf("sender", "title", "message", "date", "time")
    private var fieldsIds           = intArrayOf(R.id.sender, R.id.title, R.id.message, R.id.date, R.id.time)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MessagesFragment", "onReceive called!!!")
            val messageData = intent.getStringExtra("message_data")
            val json = JSONObject(messageData)
            json.put("sender", JSONObject(json.getString("sender")))
            val objc = MessageBean(json)
            appendMessageToList(objc,0)
            listViewAdapter!!.notifyDataSetChanged()
            Log.d("MessagesFragment", "message_data: $messageData")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // prevent view to be recreated
        retainInstance      = true
        activity            = getActivity() as? MainActivity
        listViewModel       = ArrayList()
        messagesOnTheView   = ArrayList()
        requestHandle       = MessagesRequestHandle(this)
        userProfile         = UserProfile.getInstance()

        val lbm = LocalBroadcastManager.getInstance(activity!!.applicationContext)
        lbm.registerReceiver(broadcastReceiver, IntentFilter("push_message"))

        listViewAdapter = SimpleAdapter(activity!!, listViewModel, R.layout.messages_list_item, fieldsNames, fieldsIds)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (pageView == null) {
            pageView = inflater.inflate(R.layout.messages_main, container, false)

            listView = activity!!.findViewById(R.id.list)
            listView!!.adapter = listViewAdapter
            listView!!.onItemClickListener = this
            listView!!.setOnScrollListener(this)

            floatingButton = activity!!.findViewById(R.id.send_message_action)
            floatingButton!!.setOnClickListener { _ ->
                Toast.makeText(activity!!, "Create a new message...", Toast.LENGTH_SHORT).show()
            }

            swipeRefreshWidget = activity!!.findViewById(R.id.swipe_container)
            swipeRefreshWidget!!.setOnRefreshListener(this)
        }
    }

    override fun onResume() {
        super.onResume()

        activity!!.setToolBarTitle(activity!!.getStringFromId(R.string.view_msg))
        activity!!.setNavViewItemSelected(R.id.view_msg)

        // get messages from local storage if is not loaded from previous execution
        if (messagesOnTheView!!.isEmpty()) {
            requestHandle!!.loadFromLocalStorage()
        }

        // start a request to load for new messages
        onRefresh()
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) { }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        var topRowVerticalPosition = 0

        if (listView != null && listView!!.childCount > 0)
            topRowVerticalPosition = listView!!.getChildAt(0).top

        swipeRefreshWidget!!.isEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0

        if (firstVisibleItem > 0)
            hideFloatingButton()
        else
            showFloatingButton()
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Toast.makeText(activity!!, listViewModel!![p2]["title"], Toast.LENGTH_LONG).show()
    }

    override fun requestOpened() {
        isRequestRunning = true
        activity!!.runOnUiThread({
            setProgressBarEnabled(true)
            swipeRefreshWidget!!.isRefreshing = false
        })
    }

    override fun requestFinished() {
        isRequestRunning = false
        activity!!.runOnUiThread({
            setProgressBarEnabled(false)
            swipeRefreshWidget!!.isRefreshing = false
        })
    }

    override fun requestSuccess(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?) {
        if (statusCode != 200)
            return
        activity!!.runOnUiThread({
            Toast.makeText(activity!!, response as String, Toast.LENGTH_LONG).show()
        })
    }

    override fun requestError(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?) {
        activity!!.runOnUiThread({
            Toast.makeText(activity!!, response as String, Toast.LENGTH_LONG).show()
        })
    }

    override fun setViewData(data: Any?) {
        activity!!.runOnUiThread({
            val list = data as ArrayList<*>
            for (message in list) {
                message = message as MessageBean
                if (messagesOnTheView!!.contains((message as MessageBean).id))
                    continue
                appendMessageToList(message)
            }
        })
    }

    /**
     * Handle user swipe down to request data
     * and start a new http request to check for new messages
     * @override from SwipeRefreshLayout class
     */
    override fun onRefresh() {
        requestHandle!!.request(userProfile!!.id)
    }

    private fun appendMessageToList(message: MessageBean, index: Int = -1) {
        val map = HashMap<String, String>()
        map["sender"]   = getShortSenderName(message.sender)
        map["title"]    = message.title
        map["message"]  = message.message
        map["date"]     = message.date
        map["time"]     = message.time
        if (index != -1) {
            listViewModel!!.add(index, map)
            if (listViewModel!!.size > index) {
                val view: View? = listView!!.getChildAt(index)
                if (view != null) {
                    try {
                        val animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)
                        view.startAnimation(animation)
                        if (userProfile!!.id == message.senderId) {
                            val cardView = view as CardView
                            cardView.setCardBackgroundColor(activity!!.resources.getColor(R.color.messageAlternateColor))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            listViewModel!!.add(map)
        }
        messagesOnTheView!!.add(message.id)
    }

    private fun showFloatingButton() {
        floatingButton!!.animate().setDuration(650).translationY(0F).start()
    }

    // magic happens here!
    private fun hideFloatingButton() {
        val point = Point()
        activity!!.window.windowManager.defaultDisplay.getSize(point)
        val translation = floatingButton!!.y - point.y
        floatingButton!!.animate().setDuration(800).translationYBy(-translation).start()
    }

    private fun setProgressBarEnabled(enabled: Boolean) {
        val visibilityMode = if (enabled) View.VISIBLE else View.INVISIBLE
        val progressBar = activity!!.findViewById<ProgressBar>(R.id.progressBar1)

        if (progressBar != null && progressBar.visibility != visibilityMode)
            progressBar.visibility = visibilityMode
    }

    private fun getShortSenderName(name: String):String {
        val names = name.split(" ")
        return activity!!.capitalizeString(names[0]) + " " + activity!!.capitalizeString(names.last())
    }
}
