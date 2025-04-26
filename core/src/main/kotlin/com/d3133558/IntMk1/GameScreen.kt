package com.d3133558.IntMk1



import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.net.SocketHints
//import com.badlogic.gdx.net.Socket
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import kotlin.math.abs

//TODO: https://gamefromscratch.com/libgdx-tutorial-11-tiled-maps-part-1-simple-orthogonal-maps/
//      https://gamefromscratch.com/libgdx-tutorial-11-tiled-maps-part-1-simple-orthogonal-maps/
//      https://stackoverflow.com/questions/66740979/how-to-use-socket-in-android-with-kotlin

class GameScreen(private val game: Main, private val ip: String, private val port: Int) : Screen {
    //Server config
    val HOST: String = ip
    val PORT: Int = port

    private val players = mutableMapOf<String, Sprite>()

    private lateinit var stage: Stage
    private lateinit var font: BitmapFont
    private lateinit var playerTexture: Texture
    private lateinit var playerSprite: Sprite
    private lateinit var batch: SpriteBatch
    lateinit var touchpad: Touchpad
    var touchX = 0f
    var touchY = 0f
    var lastTouchX = 0f
    var lastTouchY = 0f
    var playerX = 100f
    var playerY = 100f
    var playerSpeed = 10f
    var bRecieveMessages = true

    val buffer = ConcurrentLinkedQueue<GameMessage>()

    lateinit var socket : Socket
    lateinit var inputStream : InputStream
    lateinit var outputStream: OutputStream
    lateinit var playerID: String

    lateinit var rcvMsg : Thread
    lateinit var conMsg : Thread

    override fun show() {
        socket  =  Socket(HOST,PORT)
        inputStream = socket.getInputStream()
        outputStream = socket.getOutputStream()

        batch = SpriteBatch()

        font = BitmapFont()

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

        // Read the handshake ID message
        val prefix = kotlin.ByteArray(2)
        var prefixRead = 0
        while (prefixRead < 2) {
            val r = inputStream.read(prefix, prefixRead, 2 - prefixRead)
            if (r == -1) throw Exception("Socket closed while reading prefix")
            prefixRead += r
        }
        val totalLength = ByteBuffer.wrap(prefix).short.toInt()

        val data = kotlin.ByteArray(totalLength)
        var readSoFar = 0
        while (readSoFar < totalLength) {
            val r = inputStream.read(data, readSoFar, totalLength - readSoFar)
            if (r == -1) throw Exception("Socket closed while reading message")
            readSoFar += r
        }

        // parse PlayerID
        val buffer = ByteBuffer.wrap(data)

        val senderLength = buffer.get().toInt()
        val senderBytes = kotlin.ByteArray(senderLength)
        buffer.get(senderBytes)
        playerID = String(senderBytes, Charsets.UTF_8)

        playerSprite.color = getColourForPlayer(playerID)

        //start threads
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

        //draw all other players
        for ((id, sprite) in players) {
            if (id != playerID) {
                sprite.draw(batch)
            }
        }

        //draw names for other players
        val originalScaleX = font.data.scaleX
        val originalScaleY = font.data.scaleY
        font.data.setScale(3f) //scale up the font
        val layout = GlyphLayout()
        for ((id, sprite) in players) {
            if (id != playerID) {
                sprite.draw(batch)

                font.color = sprite.color //match name text color to sprite color
                layout.setText(font, id)
                val textX = sprite.x + (sprite.width - layout.width) / 2 //center the text horizontally
                val textY = sprite.y + sprite.height + 20f + layout.height //position the text above the sprite
                font.draw(batch, layout, textX, textY)
            }
        }
        font.data.setScale(originalScaleX, originalScaleY)

        //draw clients sprite and name
        font.data.setScale(3f)

        font.color = getColourForPlayer(playerID)
        layout.setText(font, playerID)
        val textX = playerX + (playerSprite.width - layout.width) / 2
        val textY = playerY + playerTexture.height + 20f + layout.height
        font.draw(batch, layout, textX, textY)

        playerSprite.setPosition(playerX, playerY)
        playerSprite.draw(batch)

        font.data.setScale(originalScaleX, originalScaleY)

        batch.end()

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    private fun produceMessages(clientSocket: Socket) {
        try {
            val input = clientSocket.getInputStream()
            val buffer = kotlin.ByteArray(2) //For total length

            while (true) {
                //read total length (2 Bytes)
                val prefix = kotlin.ByteArray(2)
                var prefixRead = 0
                while (prefixRead < 2) {
                    val r = input.read(prefix, prefixRead, 2 - prefixRead)
                    if (r == -1) throw Exception("Socket closed while reading prefix")
                    prefixRead += r
                }

                val totalLength = ByteBuffer.wrap(prefix).short.toInt()

                //Read the rest of the message
                val data = kotlin.ByteArray(totalLength)
                var readSoFar = 0
                while (readSoFar < totalLength) {
                    val r = input.read(data, readSoFar, totalLength - readSoFar)
                    if (r == -1) throw Exception("stream closed mid-message")
                    readSoFar += r
                }

                try {
                    val message = data.toGameMessage()
                    this.buffer.add(message)
                } catch (e: Exception) {
                    Gdx.app.error("NET", "Failed to parse message: ${e.message}")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun consumeMessages() {
        try {
            while (true) {
                val msg = buffer.poll()
                if (msg != null) {
                    if (msg.senderId != playerID) {
                        if (!players.containsKey(msg.senderId)) {
                            // if new player create a new sprite
                            val newSprite = Sprite(playerTexture)
                            //newSprite.setSize(75f, 75f)
                            newSprite.setPosition(msg.x, msg.y)

                            val playerColour = getColourForPlayer(msg.senderId)
                            newSprite.color = playerColour

                            players[msg.senderId] = newSprite
                        } else {
                            // if existing player update position
                            players[msg.senderId]?.setPosition(msg.x, msg.y)
                        }
                    }
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun input() {

    }

    private fun logic() {

        playerX = MathUtils.clamp(
            playerX + touchX * playerSpeed,
            0f,
            Gdx.graphics.width.toFloat() - playerTexture.width.toFloat()
        )
        //playerX += touchX * playerSpeed
        playerY = MathUtils.clamp(
            playerY + touchY * playerSpeed,
            0f,
            Gdx.graphics.height.toFloat() - playerTexture.height.toFloat()
        )
        //playerY += touchY * playerSpeed


        if (touchX != lastTouchX || touchY != lastTouchY) {

            val message = GameMessage(playerID, playerX, playerY, ActionType.MOVE)
            val messageBytes = message.toByteArray()

            try {
                Gdx.app.log("NET", "Sending: ${messageBytes.joinToString(" ") { it.toUByte().toString(16) }}")
                Gdx.app.log("NET", "Socket closed? ${socket.isClosed}, connected? ${socket.isConnected}")
                outputStream.write(messageBytes)
                outputStream.flush()
            } catch (e: SocketException) {
                Gdx.app.error("NET", "Socket is broken: ${e.message}")
                // maybe i can try reconnect here
            }

            lastTouchX = touchX
            lastTouchY = touchY

        }

    }

    private fun getColourForPlayer(playerId: String): Color {
        val hash = abs(playerId.hashCode())
        //arbitrary prime numbers for adding variation
        val prime1 = 37
        val prime2 = 73
        val prime3 = 137

        val r = ((hash * prime1) % 256) / 255f
        val g = ((hash * prime2) % 256) / 255f
        val b = ((hash * prime3) % 256) / 255f

        return Color(r, g, b, 1f)
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
    }

    override fun hide() {
        TODO("Not yet implemented")
    }
}
