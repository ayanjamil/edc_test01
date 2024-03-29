package com.example.edc_test01
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.edc_test01.databinding.DialogCustomBackConfirmationBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class event_detail : AppCompatActivity(),TextToSpeech.OnInitListener {
    private var restTimer: CountDownTimer? = null
    private var tts: TextToSpeech? = null // Variable for TextToSpeech
    private var player: MediaPlayer? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        val event = intent.getParcelableExtra("event",event_model::class.java)
        //reciving whateven extra was passed from main
        if (event!=null){
            eventdertailSetup(event) //sets up the details for display
            setDate(event.eventTime)//sets up date for now and event
            setupTts(event.eventName)//text to speach par
            sounfPlay() // calling the function for sound effects in the beginning
            activitybarSetup()// setting up action bar with back button support
        }
        else
            Log.d("event_details issue","when the if block is not executed in event_datials secton")
        var regBtn = findViewById<Button>(R.id.btn_register)
        regBtn.setOnClickListener {
            var intent = Intent(this,registerPage::class.java)
            startActivity(intent)
            var id= event?.eventName
            Log.d("add_dp","event naem added to db :$id")

            val dao = (application as DemoApp).db.eventDao() //definig the dao object
            addDateToDatabase(dao, id.toString())//adding the id to db via function


            Log.d("registerBtn","Register button is clicked")
        }
    }
    // this function imports all the ids and sets up the event page for every event
    private  fun eventdertailSetup(event:event_model){

        val imageView :ImageView=findViewById(R.id.event_imagview)
        val eventName :TextView=findViewById(R.id.tv_eventname)
        val descriptionContent:TextView=findViewById(R.id.tv_description_content)
        val location:TextView=findViewById(R.id.tv_location)
        val time:TextView=findViewById(R.id.tv_time)
        val registerBtn = findViewById<Button>(R.id.btn_register)
        //above part is linking event_details kind of imporitng its ids
        imageView.setImageResource(event.eventImage)
        eventName.text=event.eventName
        descriptionContent.text=event.eventDescription
        val eventlocation = event.eventLocation
        location.text="Location :$eventlocation"
        time.text="Time & Date:${event.eventTime}"

        //assigning values to the variables of event_detail activity by importing
        // information of class and passing it here


    }
    //sets up date and time for now and events time for furthure calualation
    private fun setDate(eventTime:String){

        val displaytimer = findViewById<TextView>(R.id.tv_countdown)
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)
        val hour = myCalendar.get(Calendar.HOUR)
        val min = myCalendar.get(Calendar.MINUTE)
        val sdf = SimpleDateFormat("mm:HH:dd:MM:yy", Locale.ENGLISH)
        var eventtime_calc: Date? = null
        val currenttime_calc = sdf.parse("$min:$hour:$day:${month + 1}:$year")
        // this wiill give me the current time
        Log.d("currentime", "$currenttime_calc")
        val event_time =eventTime
            //"00:13:12:2:24"
        // this event time is the time which is of the event
        eventtime_calc = sdf.parse(event_time)
        countDown(eventtime_calc,currenttime_calc,displaytimer)
    }
    // this part is responsible for countdown part
    private fun countDown(eventtime_calc: Date?, currenttime_calc: Date?,displaytimer:TextView) {
        // Countdown
        if (eventtime_calc != null && currenttime_calc != null) {
            Log.d("not null", "entered and curren times are not null")

            val diff: Long = eventtime_calc!!.time - currenttime_calc.time
            Log.d("diff", "Difference between is  $diff")

            restTimer = object : CountDownTimer(diff, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val sec = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                    val min = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                    val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)

                    val finalop = "Days:$days \nHours :$hours\nMin :$min\nSec :$sec"
                    displaytimer.text = "$finalop"
                }

                override fun onFinish() {
                    println("Time's finished!")
                }
            }
            restTimer?.start()
        }
        // Countdown
    }
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
        val result = tts!!.setLanguage(Locale.US)

        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
            // Retrieve the event
            val event = intent.getParcelableExtra("event",event_model::class.java)
            if (event != null) {
                // Speak out the event name
                val eventName = event.eventName
                speakOut(eventName.toString())
            }
            //needed to do all this just because we wanted the event name to speak up automatically without pressing any buttons
        } else {
            Log.e("TTS", "The Language specified is not supported!")
        }
    } else {
        Log.e("TTS", "Initialization Failed!")
    }
}
    public override fun onDestroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
    //sets up for talk to speach
    private fun setupTts(eventName:String){
        tts = TextToSpeech(this,this)
        val event_name = eventName
        speakOut(event_name) // it can only hold strings
        Log.d("event_name issue","the event name is $event_name")


    }
    //setup for speaking out the parsed strings
    private fun speakOut(text: String) {
        Toast.makeText(this@event_detail, "Welcome to $text", Toast.LENGTH_SHORT)
            .show()
        Log.d("tts_init","Text to speach block initiated $text")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    //making the function for enternce sound in the eventdetails part
    private fun sounfPlay(){
        try {
            val soundURI =
                Uri.parse("android.resource://com.example.edc_test01/" + R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false // Sets the player to be looping or non-looping.
            player?.start() // Starts Playback.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //sets up for actionabr
    private fun activitybarSetup(){
        val actionBar: Toolbar = findViewById(R.id.toolbarEvent)
        setSupportActionBar(actionBar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        actionBar.setNavigationOnClickListener {
            customDialogForBackButton()
        }
    }
    //setup for actionbars on click listner and use of custom dialog
    private fun customDialogForBackButton() {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.tvYes.setOnClickListener {
            this@event_detail.finish()
            customDialog.dismiss() // Dialog will be dismissed
        }
        dialogBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        //Start the dialog and display it on screen.
        customDialog.show()
    }
    private fun addDateToDatabase(eventDao: eventDao,id:String) {

        lifecycleScope.launch {
            eventDao.insert(eventEntity(id)) // uisng .insert defined in idDao interface
        }
    }

}