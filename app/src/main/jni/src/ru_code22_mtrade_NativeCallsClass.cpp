
#include "ru_code22_mtrade_NativeCallsClass.h"
#include <string.h>

#include <setjmp.h>

/*
 * ERROR HANDLING:
 *
 * The JPEG library's standard error handler (jerror.c) is divided into
 * several "methods" which you can override individually.  This lets you
 * adjust the behavior without duplicating a lot of code, which you might
 * have to update with each future release.
 *
 * Our example here shows how to override the "error_exit" method so that
 * control is returned to the library's caller when a fatal error occurs,
 * rather than calling exit() as the standard error_exit method does.
 *
 * We use C's setjmp/longjmp facility to return control.  This means that the
 * routine which calls the JPEG library must first execute a setjmp() call to
 * establish the return point.  We want the replacement error_exit to do a
 * longjmp().  But we need to make the setjmp buffer accessible to the
 * error_exit routine.  To do this, we make a private extension of the
 * standard JPEG error handler object.  (If we were using C++, we'd say we
 * were making a subclass of the regular error handler.)
 *
 * Here's the extended error handler struct:
 */

struct my_error_mgr {
  struct jpeg_error_mgr pub;	/* "public" fields */

  jmp_buf setjmp_buffer;	/* for return to caller */
};

typedef struct my_error_mgr * my_error_ptr;

/*
 * Here's the routine that will replace the standard error_exit method:
 */

METHODDEF(void)
my_error_exit (j_common_ptr cinfo)
{
  /* cinfo->err really points to a my_error_mgr struct, so coerce pointer */
  my_error_ptr myerr = (my_error_ptr) cinfo->err;

  /* Always display the message. */
  /* We could postpone this until after returning, if we chose. */
  (*cinfo->err->output_message) (cinfo);

  /* Return control to the setjmp point */
  longjmp(myerr->setjmp_buffer, 1);
}

JNIEXPORT jint JNICALL Java_ru_code22_mtrade_NativeCallsClass_convertJpegFile
  (JNIEnv *env, jclass myclass, jstring jsSrcFilename, jstring jsDestFilename, jbyteArray jbaTextData, jint textLeft, jint textTop, jint textWidth, jint textHeight, jint quality, jint scaleText)
{
	//int res=0;
	const char *srcFilename = env->GetStringUTFChars(jsSrcFilename, JNI_FALSE);
	const char *destFilename = env->GetStringUTFChars(jsDestFilename, JNI_FALSE);
	jbyte *textData = env->GetByteArrayElements(jbaTextData, JNI_FALSE);

    /* This struct contains the JPEG decompression parameters and pointers to
     * working space (which is allocated as needed by the JPEG library).
     */
    struct jpeg_decompress_struct dcinfo;
    struct jpeg_compress_struct cinfo;
    /* We use our private extension JPEG error handler.
     * Note that this struct must live as long as the main JPEG parameter
     * struct, to avoid dangling-pointer problems.
     */
    struct my_error_mgr my_jerr;
    /* This struct represents a JPEG error handler.  It is declared separately
     * because applications often want to supply a specialized error handler
     * (see the second half of this file for an example).  But here we just
     * take the easy way out and use the standard error handler, which will
     * print a message on stderr and call exit() if compression fails.
     * Note that this struct must live as long as the main JPEG parameter
     * struct, to avoid dangling-pointer problems.
     */
    struct jpeg_error_mgr jerr;
    /* More stuff */
    FILE * infile;		/* source file */
    FILE * outfile;		/* target file */
    JSAMPARRAY buffer;  /* Row buffer */
    int row_stride;		/* physical row width in image buffer */

    /* In this example we want to open the input file before doing anything else,
     * so that the setjmp() error recovery below can assume the file is open.
     * VERY IMPORTANT: use "b" option to fopen() if you are on a machine that
     * requires it in order to read binary files.
     */

    if ((infile = fopen(srcFilename, "rb")) == NULL) {
      fprintf(stderr, "can't open %s\n", srcFilename);
      return 0;
    }

    /* Step 1: allocate and initialize JPEG decompression object */

    /* We set up the normal JPEG error routines, then override error_exit. */
    dcinfo.err = jpeg_std_error(&my_jerr.pub);
    my_jerr.pub.error_exit = my_error_exit;
    /* Establish the setjmp return context for my_error_exit to use. */
    if (setjmp(my_jerr.setjmp_buffer)) {
      /* If we get here, the JPEG code has signaled an error.
       * We need to clean up the JPEG object, close the input file, and return.
       */
      jpeg_destroy_decompress(&dcinfo);
      fclose(infile);
      return 0;
    }
    /* Now we can initialize the JPEG decompression object. */
    jpeg_create_decompress(&dcinfo);
    /* We have to set up the error handler first, in case the initialization
     * step fails.  (Unlikely, but it could happen if you are out of memory.)
     * This routine fills in the contents of struct jerr, and returns jerr's
     * address which we place into the link field in cinfo.
     */
    cinfo.err = jpeg_std_error(&jerr);
    /* Now we can initialize the JPEG compression object. */
    jpeg_create_compress(&cinfo);


    /* Step 2: specify data source (eg, a file) */

    jpeg_stdio_src(&dcinfo, infile);

    /* Here we use the library-supplied code to send compressed data to a
     * stdio stream.  You can also write your own code to do something else.
     * VERY IMPORTANT: use "b" option to fopen() if you are on a machine that
     * requires it in order to write binary files.
     */
    if ((outfile = fopen(destFilename, "wb")) == NULL) {
      fprintf(stderr, "can't open %s\n", destFilename);
      return 0;
    }
    jpeg_stdio_dest(&cinfo, outfile);

    /* Step 3: read file parameters with jpeg_read_header() */

    (void) jpeg_read_header(&dcinfo, TRUE);
    /* We can ignore the return value from jpeg_read_header since
     *   (a) suspension is not possible with the stdio data source, and
     *   (b) we passed TRUE to reject a tables-only JPEG file as an error.
     * See libjpeg.doc for more info.
     */

    int image_width=dcinfo.image_width;
    int image_height=dcinfo.image_height;

    /* First we supply a description of the input image.
     * Four fields of the cinfo struct must be filled in:
     */
    cinfo.image_width = image_width; 	/* image width and height, in pixels */
    cinfo.image_height = image_height;
    cinfo.input_components = 3;		/* # of color components per pixel */
    cinfo.in_color_space = JCS_RGB; 	/* colorspace of input image */
    /* Now use the library's routine to set default compression parameters.
     * (You must set at least cinfo.in_color_space before calling this,
     * since the defaults depend on the source color space.)
     */
    jpeg_set_defaults(&cinfo);
    /* Now you can set any non-default parameters you wish to.
     * Here we just illustrate the use of quality (quantization table) scaling:
     */
    jpeg_set_quality(&cinfo, quality, TRUE /* limit to baseline-JPEG values */);


    /* Step 4: set parameters for decompression */

    /* In this example, we don't need to change any of the defaults set by
     * jpeg_read_header(), so we do nothing here.
     */

    /* Step 5: Start decompressor */

    (void) jpeg_start_decompress(&dcinfo);
    /* We can ignore the return value since suspension is not possible
     * with the stdio data source.
     */

    /* TRUE ensures that we will write a complete interchange-JPEG file.
     * Pass TRUE unless you are very sure of what you're doing.
     */
    jpeg_start_compress(&cinfo, TRUE);

    /* We may need to do some setup of our own at this point before reading
     * the data.  After jpeg_start_decompress() we have the correct scaled
     * output image dimensions available, as well as the output colormap
     * if we asked for color quantization.
     * In this example, we need to make an output work buffer of the right size.
     */
    /* JSAMPLEs per row in output buffer */
    row_stride = dcinfo.output_width * dcinfo.output_components;
    /* Make a one-row-high sample array that will go away when done with image */
    buffer = (*dcinfo.mem->alloc_sarray)
          ((j_common_ptr) &dcinfo, JPOOL_IMAGE, row_stride, 1);

    /* Step 6: while (scan lines remain to be read) */
    /*           jpeg_read_scanlines(...); */

    /* Here we use the library's state variable cinfo.output_scanline as the
     * loop counter, so that we don't have to keep track ourselves.
     */
    while (dcinfo.output_scanline < dcinfo.output_height) {
      /* jpeg_read_scanlines expects an array of pointers to scanlines.
       * Here the array is only one element long, but you could ask for
       * more than one scanline at a time if that's more convenient.
       */
      (void) jpeg_read_scanlines(&dcinfo, buffer, 1);
      /* Assume put_scanline_someplace wants a pointer and sample count. */
      // TODO
      //put_scanline_someplace(buffer[0], row_stride);

        if (dcinfo.output_scanline>=textTop&&dcinfo.output_scanline<textTop+textHeight*scaleText)
        {
            int i;
            for (i=textLeft; i<textLeft+textWidth*scaleText; i++)
            {
                // R,G,B
            	int alpha=textData[((i-textLeft)/scaleText+textWidth*((dcinfo.output_scanline-textTop)/scaleText))*4+3]&0x0FF; // не всякий случай, если байт знаковое например
                buffer[0][i*3]=((buffer[0][i*3]&0xFF)*(255-alpha)+((textData[((i-textLeft)/scaleText+textWidth*((dcinfo.output_scanline-textTop)/scaleText))*4]&0xFF)*alpha))/255;
                buffer[0][i*3+1]=((buffer[0][i*3+1]&0xFF)*(255-alpha)+((textData[((i-textLeft)/scaleText+textWidth*((dcinfo.output_scanline-textTop)/scaleText))*4+1]&0xFF)*alpha))/255;
                buffer[0][i*3+2]=((buffer[0][i*3+2]&0xFF)*(255-alpha)+((textData[((i-textLeft)/scaleText+textWidth*((dcinfo.output_scanline-textTop)/scaleText))*4+2]&0xFF)*alpha))/255;
            }
        }
        (void) jpeg_write_scanlines(&cinfo, buffer, 1);
    }

    /* Step 7: Finish decompression */

    (void) jpeg_finish_decompress(&dcinfo);
    /* We can ignore the return value since suspension is not possible
     * with the stdio data source.
     */

    /* Step 8: Release JPEG decompression object */

    /* This is an important step since it will release a good deal of memory. */
    jpeg_destroy_decompress(&dcinfo);

    /* After finish_decompress, we can close the input file.
     * Here we postpone it until after no more JPEG errors are possible,
     * so as to simplify the setjmp error logic above.  (Actually, I don't
     * think that jpeg_destroy can do an error exit, but why assume anything...)
     */
    fclose(infile);

    jpeg_finish_compress(&cinfo);
    /* After finish_compress, we can close the output file. */
    fclose(outfile);

    /* This is an important step since it will release a good deal of memory. */
    jpeg_destroy_compress(&cinfo);

    /* At this point you may want to check to see whether any corrupt-data
     * warnings occurred (test whether jerr.pub.num_warnings is nonzero).
     */

	// use your string
	//res=strlen(nativeString);
	env->ReleaseStringUTFChars(jsSrcFilename, srcFilename);
	env->ReleaseStringUTFChars(jsDestFilename, destFilename);
	env->ReleaseByteArrayElements(jbaTextData, textData, JNI_ABORT);


    /* And we're done! */
    return 1;

	//return res;
}
