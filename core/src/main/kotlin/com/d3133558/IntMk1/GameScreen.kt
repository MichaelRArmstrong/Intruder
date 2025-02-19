package com.d3133558.IntMk1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport

class GameScreen : Screen {
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
    }

    override fun render(delta: Float) {
        //logic
        playerX = MathUtils.clamp(playerX + touchX * playerSpeed,0f, Gdx.graphics.width.toFloat()  - playerTexture.width.toFloat())
        //playerX += touchX * playerSpeed
        playerY = MathUtils.clamp(playerY + touchY * playerSpeed,0f, Gdx.graphics.height.toFloat()  - playerTexture.height.toFloat())
        //playerY += touchY * playerSpeed

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

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        playerTexture.dispose()
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun hide() {
        TODO("Not yet implemented")
    }
}
