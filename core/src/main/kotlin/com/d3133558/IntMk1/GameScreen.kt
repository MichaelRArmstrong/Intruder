package com.d3133558.IntMk1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.net.SocketHints
//import com.badlogic.gdx.net.Socket
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.ByteArray
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import java.io.InputStreamReader
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

//TODO: https://gamefromscratch.com/libgdx-tutorial-11-tiled-maps-part-1-simple-orthogonal-maps/
//      https://gamefromscratch.com/libgdx-tutorial-11-tiled-maps-part-1-simple-orthogonal-maps/
//      https://stackoverflow.com/questions/66740979/how-to-use-socket-in-android-with-kotlin

class GameScreen : Screen {
    //Server config
    val HOST: String = "152.105.66.53"
    val PORT: Int = 4300

    private lateinit var stage: Stage
    private lateinit var playerTexture: Texture
    private lateinit var playerSprite: Sprite
    private lateinit var batch: SpriteBatch
    lateinit var touchpad: Touchpad
    var touchX = 0f
    var touchY = 0f
    var playerX = 100f
    var playerY = 100f
    var playerSpeed = 10f
    var bRecieveMessages = true

    val buffer = ConcurrentLinkedQueue<String>()

    val socket =  Socket(HOST,PORT)
    //val socket = Gdx.net.newClientSocket(Net.Protocol.TCP, "152.105.66.53", 4300, null)
    lateinit var rcvMsg : Thread
    lateinit var conMsg : Thread

    override fun show() {
        batch = SpriteBatch()
        playerTexture = Texture("blob.png")
        playerSprite = Sprite(playerTexture)
        playerX = Gdx.graphics.width / 2f - playerSprite.width / 2
        playerY = Gdx.graphics.height / 2f - playerSprite.height / 2
        stage = Stage(ScreenViewport(), batch)
        val touchpadSkin = Skin()
        touchpadSkin.add("touchBackground", Texture("touchpad.png"))
        touchpadSkin.add("touchKnob", Texture("touchpad-knob.png"))
        val touchpadStyle = Touchpad.TouchpadStyle()
        val touchpadBackground = touchpadSkin.getDrawable("touchBackground")
        val touchpadKnob = touchpadSkin.getDrawable("touchKnob")
        touchpadStyle.background = touchpadBackground
        touchpadStyle.knob = touchpadKnob
        touchpad = Touchpad(10f,touchpadStyle)
        touchpad.setSize(Gdx.graphics.width * 0.15f, Gdx.graphics.width * 0.15f)
        touchpad.x = 40f
        touchpad.y = 40f
        stage.addActor(touchpad)
        touchpad.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                Gdx.app.log("Touchpad","X: ${touchpad.knobPercentX}, Y: ${touchpad.knobPercentY}")
                touchX = touchpad.knobPercentX
                touchY = touchpad.knobPercentY
                //playerX += x * playerSpeed
                //playerY += y * playerSpeed
            }
        })
        Gdx.input.inputProcessor = stage
        rcvMsg = Thread { produceMessages(socket)}
        conMsg = Thread { consumeMessages()}
        rcvMsg.start()
        conMsg.start()
    }

    override fun render(delta: Float) {
        //logic
        //input();
        logic();
        draw();
    }

    private fun draw() {
        //drawing
        Gdx.gl.glClearColor(1f,0f,0f,1f)
        ScreenUtils.clear(Color.BLACK)

        batch.begin()
        playerSprite.setPosition(playerX, playerY)
        playerSprite.draw(batch)
        batch.end()

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    private fun produceMessages(clientSocket: Socket) {
        try {
            val message: kotlin.ByteArray = kotlin.ByteArray(1024)
            clientSocket.getInputStream().read(message,0,1024)
            val s : String = message.decodeToString()
            buffer.add(s)
            Thread.sleep(250) // Simulate delay

        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun consumeMessages() {
        try {
            while (true) {
                // Non-blocking: returns null if empty
                val item = buffer.poll()
                if (item != null) {
                    println(item)
                    Thread.sleep(250) // Simulate processing delay
                } else {
                    //println("Queue is empty, skipping...")
                    //Thread.sleep(200) // Avoid busy-waiting, delay
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun input() {

    }

    private fun logic() {

//        val buffer = ConcurrentLinkedQueue<Int>()
//        val producer = Thread {
//            try {
//                for (i in 1..10) {
//                    println("Producing: $i")
//                    buffer.add(i)
//                    Thread.sleep(500) // Simulate delay
//                }
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//        }
//        val consumer = Thread {
//            try {
//                while (true) {
//                    // Non-blocking: returns null if empty
//                    val item = buffer.poll()
//                    if (item != null) {
//                        println(item)
//                        Thread.sleep(1000) // Simulate processing delay
//                    } else {
//                        println("Queue is empty, skipping...")
//                        Thread.sleep(200) // Avoid busy-waiting, delay
//                    }
//                }
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//        }

//        producer.start()
//        consumer.start()
//        producer.join()
//        consumer.join()

        playerX = MathUtils.clamp(playerX + touchX * playerSpeed,0f, Gdx.graphics.width.toFloat()  - playerTexture.width.toFloat())
        //playerX += touchX * playerSpeed
        playerY = MathUtils.clamp(playerY + touchY * playerSpeed,0f, Gdx.graphics.height.toFloat()  - playerTexture.height.toFloat())
        //playerY += touchY * playerSpeed
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        playerTexture.dispose()
        rcvMsg.join()
        conMsg.join()
    }

    override fun pause() {
        TODO("Not yet implemented")
        // set boolean to false
        // join the thread
    }

    override fun resume() {
        TODO("Not yet implemented")
        // start the thread
        println("resume called")
        //val socket: Socket = Gdx.net.newClientSocket(Net.Protocol.TCP, HOST, PORT, SocketHints())
        //val rcvMsg = thread { recieveMessages(socket) }
    }

    override fun hide() {
        TODO("Not yet implemented")
    }
}
