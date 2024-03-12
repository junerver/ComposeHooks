package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.usePrevious
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

/**
 * Description: 例如一个修改用户信息的场景，我们会有如下的两个接口：
 * 1. `[get] api/user/id` 获取到用户信息
 * 2. `[put] api/user/id` 修改用户信息
 *
 * 一般来说，接口2 在提交成功后只会返回成功或者失败的信息，那么我们通常是在接口2调用成功后，再次请求接口1更新页面。
 * 这是一个非常典型的可预测修改，我们其实完全没有必要再次请求接口1，完全可以使用我们自己已有的数据来修改原来的数据源，
 * 从而减少网络请求次数、提高用户界面响应速度。
 *
 * 普通方式是：接口1 -> 接口2 -> 接口1，一发送3个请求需要等待3个请求的全部响应时间，用户界面响应较慢。
 * 如果通过乐观更新的方式则是：接口1 -> 接口2 (本地修改数据源)，只发送两次请求，只等待**一次** 接口1 的响应
 *
 *
 * For example, in a scenario of modifying user information, we will have the following two interfaces:
 * 1. `[get] apiuserid` to obtain user information
 * 2. `[put] apiuserid` to modify user information.
 *
 * Generally speaking, interface 2 will be used after the submission is successful.
 * Only success or failure information will be returned, so we usually request interface 1 to update
 * the page again after the interface 2 is called successfully.
 * This is a very typical predictable modification.
 *
 * In fact, we do not need to request interface 1 again. We can use our own existing data to
 * modify the original data source, thereby reducing the number of network requests and improving
 * the user interface response speed.
 *
 * The common method is: interface 1 -> interface 2 -> interface 1. Once you send 3 requests,
 * you need to wait for the full response time of the 3 requests, and the user interface responds slowly.
 *
 * If the optimistic update method is used: interface 1 -> interface 2 (local modification data source),
 * only two requests are sent and only one response from interface 1 is waited.
 * @author Junerver
 * date: 2024/3/12-14:30
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun Mutate() {
    val (input, setInput) = useState("")
    val (userInfo, loading, _, _, mutate) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams = arrayOf("junerver")
        }
    )
    fun mockFnChangeName(newName: String) {
        /**
         * request some api to change user name.
         * Generally, it should be the request function obtained
         * by another [useRequest] destructuring statement that configures manual requests.
         */
    }

    /**
     * 使用 [usePrevious] 保存mutate之前的状态，用于回滚
     * Use [usePrevious] to save the state before mutate for rollback
     */
    val previous = usePrevious(present = userInfo)
    Surface {
        Column {
            OutlinedTextField(value = input, onValueChange = setInput)
            Row {
                TButton(text = "changeName") {
                    mockFnChangeName(input)
                    /**
                     * 调用 [mutate] 乐观更新
                     * Call [mutate] for optimistic updates
                     */
                    if (userInfo.asBoolean()) {
                        // request user info success
                        mutate {
                            it!!.copy(name = input)
                        }
                    }
                    setInput("")
                }

                TButton(text = "rollback") {
                    /*真实案例应该是监听changeName的onError回调，如果失败则回滚*/
                    /*A real case should be to listen to the onError callback of changeName, and roll back if it fails.*/
                    previous?.let { mutate { _ -> it } }
                }
            }

            if (loading) {
                Text(text = "Loading ...")
            } else {
                Text(text = "$userInfo")
            }
        }
    }
}
