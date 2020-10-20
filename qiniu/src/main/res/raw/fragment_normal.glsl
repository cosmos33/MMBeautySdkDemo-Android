precision mediump float;
uniform sampler2D inputexture;
varying vec2 vTextureCoord;
void main(){
    gl_FragColor = texture2D(inputexture, vTextureCoord );
}

