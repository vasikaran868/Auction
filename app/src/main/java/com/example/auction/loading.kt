
import com.example.auction.login.login_fragmentDirections
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class loading : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!
    var ds_username:String? = null
    var ds_password:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("MyActivity","loading frag created")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val arg_username = arguments?.get("username")
        val arg_password = arguments?.get("password")
        viewModel.initialise_data_store(requireContext())
        val loading_progress_bar = binding.loadingProgressBar
        val loading_tv = binding.loadingTv
        loading_progress_bar.max = 100

        viewModel.is_connected_to_server.observe(viewLifecycleOwner,{
            if (it == true){
                loading_progress_bar.setProgress(75)
                loading_tv.text = "LOGGING IN..."
                if (viewModel.is_collector_initialised["loading"] == false){
                    lifecycleScope.launch {
                        Log.v("MyActivity","loading collector initialised")
                        viewModel.is_collector_initialised["loading"]=true
                        viewModel.shared_msg.collect(){
                            val params = split_to_list(it)
                            Log.v("MyActivity","from loading: $it")
                            if (params[0] == "logged_in"){
                                viewModel.current_user = User(username= params[1], password =  params[2], email = params[3], xp = params[4].toInt(), match_played = params[5].toInt(), match_won = params[6].toInt(), avg_points = params[7].toFloat())
                                val action=loadingDirections.actionLoadingToDashboard()
                                activity?.findNavController(R.id.loading_page)?.navigate(action)
                            }
                            else{
                                val action = loadingDirections.actionLoadingToLoginFragment()
                                activity?.findNavController(R.id.loading_page)?.navigate(action)
                            }
                        }
                    }
                }
                if (arg_username == null && arg_password == null){
                    viewModel.datastore.usernameFlow.asLiveData().observe(viewLifecycleOwner,{
                        Log.v("MyActivity","user: $it")
                        ds_username = it
                    })
                    viewModel.datastore.passwordFlow.asLiveData().observe(viewLifecycleOwner,{
                        Log.v("MyActivity","password: $it,,,$ds_username")
                        ds_password = it
                        if (ds_username != "" && ds_password != ""){
                            val msg = "0 $ds_username $ds_password"
                            viewModel.send_to_Server(msg)
                        }
                        else if (ds_username == "" && ds_password == ""){
                            val action = loadingDirections.actionLoadingToLoginFragment()
                            activity?.findNavController(R.id.loading_page)?.navigate(action)
                        }
                    })
                }
            }
        })
        Log.v("MyActivity","arg $arg_username")
        Log.v("MyActivity","arg $arg_password")
        if (arg_username != null  && arg_password != null){
            lifecycleScope.launch {
                Log.v("MyActivity","abcdefgh")
                viewModel.datastore.savelogindetailsToPreferencesStore(arg_username.toString(),arg_password.toString(),requireContext())
                val action = loadingDirections.actionLoadingToDashboard()
                activity?.findNavController(R.id.loading_page)?.navigate(action)
            }
        }
        loading_progress_bar.setProgress(25)
        loading_tv.text = "CONNECTING TO SERVER..."

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Toast.makeText(context, "back button pressed", Toast.LENGTH_SHORT).show()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["loading"] = false
        Log.v("MyActivity","loading fragment destroyed")
    }

    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())
}
