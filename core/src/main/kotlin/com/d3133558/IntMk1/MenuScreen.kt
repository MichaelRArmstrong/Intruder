package com.d3133558.IntMk1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

class MenuScreen (private val game: Main) : Screen{

    private val stage = Stage(ScreenViewport())
    private val skin = Skin(Gdx.files.internal("skin/uiskin.json"))

    private lateinit var ipField: TextField
    //private lateinit var portField: TextField
    private lateinit var connectButton: TextButton

    override fun show() {
        Gdx.input.inputProcessor = stage

        val table = Table()
        table.setFillParent(true)
        table.setPosition(0f,300f)

        val textFieldStyle = skin.get(TextField.TextFieldStyle::class.java).also {
            it.font.data.setScale(4f)
            it.background.bottomHeight = 20f // Adjust this value
            it.font.data.down = -20f // Adjust this value
        }

        ipField = TextField("(IP ADDRESS)", skin)
        ipField.style.font.data.setScale(4f)

        //portField = TextField("4300", skin)
        //portField.style.font.data.setScale(4f)

        connectButton = TextButton("Connect", skin)
        connectButton.style.font.data.setScale(4f)

        table.add(Label("Server IP", skin))
        table.add(ipField).width(600f).height(100f).pad(40f)
        table.row()

        //table.add(Label("Port", skin))
        //table.add(portField).width(600f).height(100f).pad(40f)
        //table.row()

        table.add(connectButton).colspan(2).width(300f).height(100f).padTop(30f)

        connectButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val ip = ipField.text
                //val port = portField.text.toIntOrNull() ?: 4300 // Default fallback
                val port = 4300 //default
                game.setScreen(GameScreen(game, ip, port))
            }
        })
        stage.addActor(table)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun hide() {
        Gdx.input.inputProcessor = null // Remove the input processor
    }

    override fun dispose() {
        stage.dispose()
        skin.dispose()
    }
}
