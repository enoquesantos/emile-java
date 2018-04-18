package gsort.pos.engsisubiq.EmileMobile

interface IRequestHttpFragment {
    fun requestOpened()
    fun requestFinished()
    fun requestError(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?)
    fun requestSuccess(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?)
    fun setViewData(data: Any?)
}
