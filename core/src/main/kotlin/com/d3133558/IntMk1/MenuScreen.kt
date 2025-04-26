package com.d3133558.IntMk1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

class MenuScreen (private val game: Main) : Screen{

    private val stage = Stage(ScreenViewport())
    private val skin = Skin(Gdx.files.internal("skin/uiskin.json"))

    private lateinit var ipField: TextField
    private lateinit var portField: TextField
    private lateinit var connectButton: TextButton

    override fun show() {
        Gdx.input.inputProcessor = stage

        val table = Table()
        table.setFillParent(true)

        ipField = TextField("192.168.0.64", skin)
        portField = TextField("4300", skin)
        connectButton = TextButton("Connect", skin)

        table.add(Label("Server IP", skin))
        table.add(ipField).width(200f).pad(10f)
        table.row()

        table.add(Label("Port", skin))
        table.add(portField).width(200f).pad(10f)
        table.row()

        table.add(connectButton).colspan(2).pad(20f)

        connectButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val ip = ipField.text
                val port = portField.text.toIntOrNull() ?: 4300 // Default fallback

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
