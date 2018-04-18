package gsort.pos.engsisubiq.EmileMobile

import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AlertDialog
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.MenuItem
import android.view.WindowManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

import java.lang.IllegalStateException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    /**
     * When some fragment set toolbar to back state, this property will be true
     * and will be used in onBackPressed method to decide
     * if enable home button in Toolbar
     */
    private var isToolbarBackButtonEnabled: Boolean = false

    /**
     * Keep the name of current fragment on stack
     */
    private var currentFragmentTag: String = ""

    /**
     * Get a instance of LocalStorage class
     */
    private var localStorage: LocalStorage? = null

    /**
     * Keeps a instance of Android AlertDialog class
     */
    private var dialog: AlertDialog? = null

    /**
     * A Java HashMap with application fragments tags and instance
     */
    private var fragments: HashMap<Int, Fragment>? = null

    /**
     * Initial method called by Application when activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localStorage = LocalStorage.getInstance()
        localStorage!!.initialize(this)

        val userProfile = UserProfile.getInstance()
        userProfile.loadFromLocalStorage()

        setContentView(R.layout.activity_main)

        // create all fragments
        fragments = HashMap()
        fragments!![R.id.view_msg]    = MessagesFragment()
        fragments!![R.id.send_msg]    = SendMessageFragment()
        fragments!![R.id.my_profile]  = UserProfileFragment()

        when {
            userProfile.isLoggedIn -> {
                loadInitialFragment()
                val nt = Notifications.getInstance()
                nt.initialize(this)
            }
            else -> {
                setToolBarEnabled(false)
                setDrawerEnabled(false)
                addFragment(LoginFragment(), "login")
            }
        }
    }

    /**
     * Handle android physical back button
     */
    override fun onBackPressed() {
        if (isToolbarBackButtonEnabled)
            setToolBarHomeButton()
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount <= 1 -> moveTaskToBack(false)
            else -> super.onBackPressed()
        }
    }

    /**
     * Handle the drawer menu clicks by opening the corresponding fragment
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var tag = ""
        var fragment: Fragment? = null
        when (item.itemId) {
            R.id.view_msg -> {
                tag = "messages_view"
                fragment = fragments!![R.id.view_msg]
            }
            R.id.send_msg -> {
                tag = "send_message"
                fragment = fragments!![R.id.send_msg]
            }
            R.id.my_profile -> {
                tag = "user_profile"
                fragment = fragments!![R.id.my_profile]
            }
            R.id.exit_to_app -> {
                // set user logged in to false
                UserProfile.getInstance().logout()

                // disable toolbar and drawer
                setToolBarEnabled(false)
                setDrawerEnabled(false)

                // remove all tags from stack
                clearFragmentsStack()

                tag = "user_login"
                fragment = LoginFragment()
            }
        }

        if (currentFragmentTag != tag)
            addFragment(fragment, tag)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun showAlertDialog(title: String, message: String?, positiveBtnText: String? = "OK", confirmCallback: (() -> Unit)? = null, negativeBtnText: String? = "", cancelCallback: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveBtnText, { _, _ -> confirmCallback?.invoke() })

        if (negativeBtnText != "")
            builder.setNegativeButton(negativeBtnText, DialogInterface.OnClickListener { _, _ -> cancelCallback?.invoke() })

        dialog = builder.create()
        dialog!!.window.attributes.windowAnimations = R.style.DialogAnimation
        dialog!!.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog!!.show()
    }

    fun loadInitialFragment(clearStack: Boolean? = false) {
        // set drawer menu listener clicks
        initializeWidgets()

        if (clearStack!!)
            clearFragmentsStack()

        // load initial fragment
        addFragment(fragments!![R.id.view_msg], null)
        currentFragmentTag = "messages_view"
    }

    fun addFragment(fragment: Fragment? = null, tag: String?) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.content_frame, fragment)
                .addToBackStack(tag)
                .commit()
        supportFragmentManager.executePendingTransactions()
        currentFragmentTag = tag ?: ""
    }

    fun setNavViewItemSelected(id: Int) {
        nav_view.setCheckedItem(id)
    }

    fun setToolBarHomeButton() {
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setDisplayShowHomeEnabled(false)

        toolbar!!.setNavigationIcon(R.drawable.ic_menu)
        toolbar!!.setNavigationOnClickListener({
            drawer_layout.openDrawer(GravityCompat.START)
        })

        setDrawerEnabled(true)

        isToolbarBackButtonEnabled = false
    }

    fun setToolBarBackButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        setDrawerEnabled(false)

        toolbar!!.setNavigationOnClickListener(View.OnClickListener {
            onBackPressed()
            setToolBarHomeButton()
        })

        isToolbarBackButtonEnabled = true
    }

    fun setToolBarTitle(title: String) {
        toolbar!!.title = title
    }

    private fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        drawer_layout.setDrawerLockMode(lockMode)
        toggle.syncState()
        toggle.onDrawerStateChanged(lockMode)
        toggle.isDrawerIndicatorEnabled = enabled
    }

    private fun setToolBarEnabled(enabled: Boolean) {
        val visibilityMode = if (enabled) View.VISIBLE else View.GONE
        val appBarLayout: AppBarLayout = findViewById(R.id.appBarLayout)

        if (appBarLayout.visibility != visibilityMode)
            appBarLayout.visibility = visibilityMode

        if (toolbar.visibility != visibilityMode)
            toolbar.visibility = visibilityMode

        setSupportActionBar(toolbar)
    }

    private fun initializeWidgets() {
        setToolBarEnabled(true)
        setDrawerEnabled(true)
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun clearFragmentsStack() {
        var i = 0
        val fragmentsSize = supportFragmentManager.backStackEntryCount
        while (i < fragmentsSize) {
            try {
                supportFragmentManager!!.popBackStackImmediate()
                supportFragmentManager!!.beginTransaction().disallowAddToBackStack().commitAllowingStateLoss()
                i++
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}
