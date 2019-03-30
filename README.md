# locationRecorder
Android app "Location Recorder"

Distributed through Google Play Store: https://play.google.com/store/apps/details?id=com.melin.jonas.mytestapplication

Overview: 
 - Start the time tracking App, the rest is automatic!
 - You will get a report of how much time you spent at work or at the gym
 - Your integrity is important! The app can and will not share your positions outside your phone! The log cannot 
   be copy-pasted. Positions are named and never displayed as latitude/longitude! you cannot share anything to facebook ;-)
 - Power effective! Using only passive positioning. Leave it running without worrying about draining the battery.
 - User tell-back is presented in the Android notification field. Management is performed through the app.
 - No adds. 100% free! 100% anonymous.

Usage:
 - Start the app. Do nothing.
 - The app will print the stable positions in the report view and current status in phone notifications bar.
 - At any time, rename your positions that you want to record and report in the future, e.g. 
   work or gym (By renaming a position to an empty name, those positions will be cleared)
 - Press \"clear unnamed\" to remove dummy positions that you never gave a name.
 - Press \"clear log\" to clear and reset your time report log.
 - Press \"clear all\" to do factory reset of app. You once again must name e.g. work or gym.

Algorithm:
 - Stable positions means: A position of radius 500m (up to 900m in case of bad accuracy) where you spent 
   at least 20 minutes in. If either of these are not met all information about that position will be discarded.
 - Hysteres: You may leave a stable position for about 10 minutes and then go back, and it will be handled as 
   one continous recording.

Known limitations:
 - Using only passive positioning means it is designed to be used in urban areas with many network cells,
   but it will still work also with only one cell, then with less accuracy in the time reports.
